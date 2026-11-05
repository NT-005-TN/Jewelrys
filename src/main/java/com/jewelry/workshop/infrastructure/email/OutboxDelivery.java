package com.jewelry.workshop.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxDelivery {
    private final JdbcTemplate jdbc;
    private final JavaMailSender sender;
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}")
    private String from;

    @Scheduled(fixedDelayString = "${app.mail.poll-ms:5000}")
    @Transactional
    public void deliver() {
        var rows = jdbc.queryForList("select * from email_outbox where sent_at is null and attempts < 10 and next_attempt_at <= current_timestamp order by id limit 10 for update skip locked");
        for (var row : rows) {
            long id = ((Number) row.get("id")).longValue();
            int attempts = ((Number) row.get("attempts")).intValue();
            try {
                var message = sender.createMimeMessage();
                var helper = new MimeMessageHelper(message, "UTF-8");
                if (!from.isBlank()) helper.setFrom(from);
                helper.setTo((String) row.get("recipient"));
                helper.setSubject((String) row.get("subject"));
                helper.setText((String) row.get("body"), true);
                sender.send(message);
                jdbc.update("update email_outbox set sent_at=current_timestamp, body='', attempts=attempts+1 where id=?", id);
            } catch (Exception ex) {
                int delaySeconds = Math.min(3600, 30 * (1 << attempts));
                jdbc.update("update email_outbox set attempts=attempts+1, next_attempt_at=current_timestamp + (? * interval '1 second') where id=?", delaySeconds, id);
                log.warn("Email outbox delivery failed: id={}, attempt={}, type={}", id, attempts + 1, ex.getClass().getSimpleName());
            }
        }
    }
}
