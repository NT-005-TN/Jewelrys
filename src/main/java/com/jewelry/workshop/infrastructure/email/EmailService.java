package com.jewelry.workshop.infrastructure.email;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    @org.springframework.beans.factory.annotation.Value("${app.public-base-url:http://localhost:8080/api}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token){
        String link = baseUrl + "/auth/verify-email?token=" + token;
        String subject = "Подтверждение email для ювелирной мастерской";
        String htmlContent = "<html><body>" +
                "<h2>Добро пожаловать!</h2>" +
                "<p>Для подтверждения вашего email перейдите по ссылке:</p>" +
                "<a href=\"" + link + "\">Подтвердить email</a>" +
                "<p>Ссылка действительна в течение 24 часов.</p>" +
                "</body></html>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendWelcomeEmail(String to) {
        String subject = "Добро пожаловать!";
        String htmlContent = "<html><body>" +
                "<h2>Ваш email успешно подтверждён!</h2>" +
                "<p>Теперь вы можете войти в систему и делать заказы.</p>" +
                "</body></html>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        jdbc.update("insert into email_outbox(recipient, subject, body) values (?, ?, ?)", to, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = baseUrl + "/auth/reset-password-form#token=" + token;
        String subject = "Сброс пароля для ювелирной мастерской";
        String htmlContent = "<html><body>" +
                "<h2>Сброс пароля</h2>" +
                "<p>Для сброса пароля перейдите по ссылке:</p>" +
                "<a href=\"" + link + "\">Сбросить пароль</a>" +
                "<p>Ссылка действительна в течение 1 часа.</p>" +
                "</body></html>";

        sendHtmlEmail(to, subject, htmlContent);
    }
}
