package com.jewelry.workshop.config;

import com.jewelry.workshop.security.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class AuditContext {
    private final JdbcTemplate jdbc;

    @Before("execution(public * com.jewelry.workshop.service.impl.*.*(..))")
    public void setActor() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()) return;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl user
                ? user.getId().toString() : "";
        // Transaction-local setting cannot leak into a connection pool's next borrower.
        jdbc.queryForObject("select set_config('app.user_id', ?, true)", String.class, actor);
    }
}
