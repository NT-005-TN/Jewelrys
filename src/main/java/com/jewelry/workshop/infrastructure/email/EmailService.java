package com.jewelry.workshop.infrastructure.email;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Async("mailTaskExecutor")
    public void sendVerificationEmail(String to, String token){
        String link = UriComponentsBuilder.fromUriString(publicBaseUrl)
                .path("/auth/verify-email")
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
        String subject = "Подтверждение email для ювелирной мастерской";
        String htmlContent = "<html><body>" +
                "<h2>Добро пожаловать!</h2>" +
                "<p>Для подтверждения вашего email перейдите по ссылке:</p>" +
                "<a href=\"" + HtmlUtils.htmlEscape(link) + "\">Подтвердить email</a>" +
                "<p>Ссылка действительна в течение 24 часов.</p>" +
                "</body></html>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async("mailTaskExecutor")
    public void sendWelcomeEmail(String to) {
        String subject = "Добро пожаловать!";
        String htmlContent = "<html><body>" +
                "<h2>Ваш email успешно подтверждён!</h2>" +
                "<p>Теперь вы можете войти в систему и делать заказы.</p>" +
                "</body></html>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent){
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Email успешно отправлен");
        } catch (MessagingException | MailException e) {
            log.error("Не удалось отправить email: {}", e.getClass().getSimpleName());
        }
    }

    @Async("mailTaskExecutor")
    public void sendPasswordResetEmail(String to, String token) {
        String link = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/reset-password")
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
        String subject = "Сброс пароля для ювелирной мастерской";
        String htmlContent = "<html><body>" +
                "<h2>Сброс пароля</h2>" +
                "<p>Для сброса пароля перейдите по ссылке:</p>" +
                "<a href=\"" + HtmlUtils.htmlEscape(link) + "\">Сбросить пароль</a>" +
                "<p>Ссылка действительна в течение 1 часа.</p>" +
                "</body></html>";

        sendHtmlEmail(to, subject, htmlContent);
    }
}
