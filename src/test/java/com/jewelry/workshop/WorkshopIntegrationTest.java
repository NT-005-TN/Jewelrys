package com.jewelry.workshop;

import com.jewelry.workshop.domain.model.dto.auth.*;
import com.jewelry.workshop.domain.model.dto.order.*;
import com.jewelry.workshop.domain.model.dto.orderitem.*;
import com.jewelry.workshop.domain.model.entity.*;
import com.jewelry.workshop.domain.repository.*;
import com.jewelry.workshop.service.interfaces.*;
import com.jewelry.workshop.infrastructure.email.OutboxDelivery;
import com.jewelry.workshop.presentation.exception.InvalidTokenException;
import com.jewelry.workshop.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.mail.poll-ms=3600000", "management.health.mail.enabled=false"})
@AutoConfigureMockMvc
class WorkshopIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        if (System.getenv("TEST_DATABASE_URL") != null) {
            r.add("spring.datasource.url", () -> System.getenv("TEST_DATABASE_URL"));
            r.add("spring.datasource.username", () -> "workshop_test");
            r.add("spring.datasource.password", () -> "");
            r.add("app.jwt.secret", () -> Base64.getEncoder().encodeToString(new byte[64]));
            return;
        }
        postgres.start(); redis.start();
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        r.add("app.jwt.secret", () -> Base64.getEncoder().encodeToString(new byte[64]));
    }
    @AfterAll static void stopContainers() {
        if (redis.isRunning()) redis.stop();
        if (postgres.isRunning()) postgres.stop();
    }
    @Autowired OrderService orders;
    @Autowired AuthService auth;
    @Autowired UserService userService;
    @Autowired ReportService reports;
    @Autowired UserRepository users;
    @Autowired ClientRepository clients;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orderRepository;
    @Autowired PasswordUtil passwords;
    @Autowired JdbcTemplate jdbc;
    @Autowired RateLimitService limits;
    @Autowired OutboxDelivery outbox;
    @Autowired PlatformTransactionManager manager;
    @Autowired MockMvc mvc;
    @MockitoBean JavaMailSender sender;
    Long clientId, adminId;
    String email;

    @BeforeEach void fixture() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        jdbc.update("delete from email_outbox");
        email = UUID.randomUUID() + "@test.dev";
        User user = new User();
        user.setEmail(email); user.setUsername(UUID.randomUUID().toString());
        user.setPasswordHash(passwords.encode("TestPassword123!")); user.setRole(User.Role.CLIENT);
        user.setEnabled(true); user.setEmailVerified(true); users.saveAndFlush(user);
        Client client = new Client(); client.setUser(user); client.setFirstName("Test"); client.setLastName("Client");
        clientId = clients.saveAndFlush(client).getId();
        User admin = new User(); admin.setEmail(UUID.randomUUID()+"@test.dev");
        admin.setUsername(UUID.randomUUID().toString()); admin.setPasswordHash("unused");
        admin.setRole(User.Role.ADMIN); admin.setEnabled(true); admin.setEmailVerified(true);
        adminId = users.saveAndFlush(admin).getId();
    }
    Product product(int stock) {
        Product p = new Product(); p.setName("Ring"); p.setType("RING"); p.setWeight(BigDecimal.ONE);
        p.setPrice(new BigDecimal("1200.00")); p.setInStock(stock); p.setIsAvailable(true);
        return products.saveAndFlush(p);
    }
    OrderCreateDTO request(Long id, int quantity) {
        OrderItemCreateDTO item = new OrderItemCreateDTO(); item.setProductId(id); item.setQuantity(quantity);
        OrderCreateDTO dto = new OrderCreateDTO(); dto.setItems(List.of(item)); return dto;
    }
    List<Boolean> race(Supplier<Boolean> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch gate = new CountDownLatch(1);
        try {
            Callable<Boolean> run = () -> { gate.await(); return task.get(); };
            var a = pool.submit(run); var b = pool.submit(run); gate.countDown();
            return List.of(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); }
    }

    @Test void concurrentPurchasesCannotOversellAndOrderNumberIsReturned() throws Exception {
        Product p = product(1);
        var results = race(() -> {
            try { assertNotNull(orders.createOrder(clientId, request(p.getId(), 1)).getOrderNumber()); return true; }
            catch (IllegalArgumentException expected) { return false; }
        });
        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
        assertEquals(0, products.findById(p.getId()).orElseThrow().getInStock());
    }
    @Test void concurrentCancellationRestoresStockExactlyOnce() throws Exception {
        Product p = product(2);
        Long id = orders.createOrder(clientId, request(p.getId(), 1)).getId();
        race(() -> { orders.cancelOrder(id, clientId); return true; });
        assertEquals(2, products.findById(p.getId()).orElseThrow().getInStock());
        assertEquals(Order.STATUS_CANCELLED, orderRepository.findById(id).orElseThrow().getStatus());
    }
    @Test void failedMultiItemOrderRollsBackStock() {
        Product a = product(1), b = product(0);
        OrderCreateDTO dto = request(a.getId(), 1);
        dto.setItems(List.of(dto.getItems().get(0), request(b.getId(), 1).getItems().get(0)));
        assertThrows(IllegalArgumentException.class, () -> orders.createOrder(clientId, dto));
        assertEquals(1, products.findById(a.getId()).orElseThrow().getInStock());
    }
    AuthResponseDTO login() {
        LoginRequestDTO dto = new LoginRequestDTO(); dto.setEmail(email); dto.setPassword("TestPassword123!");
        return auth.login(dto);
    }
    @Test void refreshIsSingleUseUnderConcurrency() throws Exception {
        String token = login().getRefreshToken();
        var results = race(() -> {
            RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO(); dto.setRefreshToken(token);
            try { auth.refreshToken(dto); return true; } catch (InvalidTokenException expected) { return false; }
        });
        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
    }
    @Test void logoutImmediatelyRevokesAccessToken() throws Exception {
        AuthResponseDTO session = login();
        mvc.perform(get("/clients/me").header("Authorization", "Bearer " + session.getAccessToken())).andExpect(status().isOk());
        auth.logout(session.getRefreshToken());
        mvc.perform(get("/clients/me").header("Authorization", "Bearer " + session.getAccessToken())).andExpect(status().isUnauthorized());
    }
    @Test void completedOrderCanBeDeliveredAndCancelledSalesAreExcluded() {
        Product p = product(2);
        Long id = orders.createOrder(clientId, request(p.getId(), 1)).getId();
        orders.changeStatus(id, Order.STATUS_COMPLETED, adminId);
        orders.changeStatus(id, Order.STATUS_DELIVERED, adminId);
        assertEquals(Order.STATUS_DELIVERED, orderRepository.findById(id).orElseThrow().getStatus());
        Instant start = Instant.now().minusSeconds(3600), end = Instant.now().plusSeconds(3600);
        var before = reports.getSalesReport(start, end).getTotalRevenue();
        orders.changeStatus(id, Order.STATUS_CANCELLED, adminId);
        assertEquals(0, before.subtract(reports.getSalesReport(start, end).getTotalRevenue()).compareTo(new BigDecimal("1200.00")));
        assertDoesNotThrow(() -> reports.getProductReport(start, end));
        assertDoesNotThrow(() -> reports.getClientReport(start, end));
    }
    @Test void reportIncludesProductsWithoutAnySales() {
        Product p = product(1);
        var rows = products.getProductSalesStatistics(Instant.EPOCH, Instant.now().plusSeconds(3600),
                org.springframework.data.domain.Pageable.unpaged());
        var row = rows.stream().filter(r -> ((Product) r[0]).getId().equals(p.getId())).findFirst().orElseThrow();
        assertEquals(0, ((Number) row[2]).longValue());
        assertEquals(0, ((BigDecimal) row[3]).compareTo(BigDecimal.ZERO));
    }
    @Test void rateLimitLuaIsAtomic() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(System.getenv("TEST_DATABASE_URL") == null, "Local PostgreSQL mode has no Redis");
        String key = UUID.randomUUID().toString();
        assertEquals(1, race(() -> limits.isAllowed(key, 1, 1)).stream().filter(Boolean::booleanValue).count());
    }
    @Test void outboxRetriesWithoutRollingBackBusinessData() {
        String recipient = UUID.randomUUID()+"@test.dev";
        jdbc.update("insert into email_outbox(recipient,subject,body) values (?, 'subject', 'body')", recipient);
        when(sender.createMimeMessage()).thenThrow(new IllegalStateException("SMTP unavailable"));
        outbox.deliver();
        assertEquals(1, jdbc.queryForObject("select attempts from email_outbox where recipient=?", Integer.class, recipient));
        assertNull(jdbc.queryForObject("select sent_at from email_outbox where recipient=?", Object.class, recipient));
    }
    @Test void successfulOutboxDeliveryClearsSensitiveBody() {
        String recipient = UUID.randomUUID()+"@test.dev";
        jdbc.update("insert into email_outbox(recipient,subject,body) values (?, 'subject', 'one-time-secret')", recipient);
        when(sender.createMimeMessage()).thenAnswer(call -> new jakarta.mail.internet.MimeMessage(
                jakarta.mail.Session.getInstance(new java.util.Properties())));
        outbox.deliver();
        assertNotNull(jdbc.queryForObject("select sent_at from email_outbox where recipient=?", Object.class, recipient));
        assertEquals("", jdbc.queryForObject("select body from email_outbox where recipient=?", String.class, recipient));
    }
    @Test void resetPageAndAnonymousAccessAreCorrect() throws Exception {
        mvc.perform(get("/auth/reset-password-form")).andExpect(status().isOk());
        mvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }

    @Test void roleChangePreservesOrderHistory() {
        Product p = product(1);
        Long id = orders.createOrder(clientId, request(p.getId(), 1)).getId();
        Long userId = users.findByEmail(email).orElseThrow().getId();
        userService.updateUserRole(userId, "SELLER");
        userService.updateUserRole(userId, "CLIENT");
        assertTrue(orderRepository.existsById(id));
        assertEquals(clientId, clients.findByUserId(userId).orElseThrow().getId());
    }

    @Test void registrationVerificationAndResetUseTransactionalOutbox() throws Exception {
        String address = UUID.randomUUID()+"@test.dev";
        RegisterRequestDTO dto = new RegisterRequestDTO(); dto.setEmail(address);
        dto.setFirstName("Иван"); dto.setLastName("Иванов");
        dto.setPassword("TestPassword123!"); dto.setConfirmPassword("TestPassword123!");
        auth.register(dto);
        User registered = users.findByEmail(address).orElseThrow();
        assertFalse(registered.isEnabled());
        assertTrue(registered.getUsername().startsWith("иван.иванов"));
        assertEquals(1, jdbc.queryForObject("select count(*) from email_outbox where recipient=?", Integer.class, address));
        auth.verifyEmail(registered.getVerificationToken());
        assertThrows(InvalidTokenException.class, () -> auth.verifyEmail(registered.getVerificationToken()));
        email = address;
        AuthResponseDTO session = login();
        auth.initiatePasswordReset(address);
        String reset = users.findByEmail(address).orElseThrow().getPasswordResetToken();
        auth.resetPassword(reset, "ChangedPassword123!");
        assertThrows(InvalidTokenException.class, () -> auth.resetPassword(reset, "ChangedAgain123!"));
        mvc.perform(get("/clients/me").header("Authorization", "Bearer " + session.getAccessToken())).andExpect(status().isUnauthorized());
        assertTrue(passwords.matches("ChangedPassword123!", users.findByEmail(address).orElseThrow().getPasswordHash()));
    }

    @Test void auditIsTransactionalAndDoesNotCopyPassword() {
        User user = users.findByEmail(email).orElseThrow();
        String values = jdbc.queryForObject("select new_values::text from audit_log where table_name='users' and record_id=? order by id desc limit 1", String.class, user.getId());
        assertFalse(values.contains("password_hash"));
        assertFalse(values.contains("refresh_token_hash"));
        int before = jdbc.queryForObject("select count(*) from audit_log", Integer.class);
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(manager).execute(status -> {
            product(1); throw new IllegalStateException("rollback");
        }));
        assertEquals(before, jdbc.queryForObject("select count(*) from audit_log", Integer.class));
    }
}
