package com.undercontroll.application.usecase.impl;

import com.undercontroll.application.port.EmailService;
import com.undercontroll.application.port.EmailTemplateLoader;
import com.undercontroll.application.usecase.UserCreatedPort;
import com.undercontroll.domain.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserCreatedImpl implements UserCreatedPort {

    private static final String HTML_NAME = "user_created.html";

    private final EmailService emailService;
    private final EmailTemplateLoader emailTemplateLoader;

    private final String year = String.valueOf(LocalDateTime.now().getYear());
    private final String websiteUrl = "https://www.comercialirmaospelluci.com.br";
    private final String contactUrl = "mailto:comercialirmaopeluci@gmail.com";

    @Override
    public void execute(UserCreatedEvent event) {
        if (event == null || event.email() == null || event.email().isBlank()) {
            log.info("Skipping welcome email, invalid event or empty email.");
            return;
        }

        String template = buildTemplate(event);

        emailService.sendEmail(
                event.email(),
                "Bem-vindo(a) a Comercial Irmaos Pelluci",
                template
        );

        log.info("Welcome email sent to {}", event.email());
    }

    private String buildTemplate(UserCreatedEvent event) {
        String template = emailTemplateLoader.load(HTML_NAME);

        return template
                .replace("{{name}}", event.name() != null ? event.name() : "Cliente")
                .replace("{{email}}", event.email() != null ? event.email() : "")
                .replace("{{createdAt}}", formatDateTime(event.createdAt()))
                .replace("{{year}}", year)
                .replace("{{websiteUrl}}", websiteUrl)
                .replace("{{contactUrl}}", contactUrl);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");
        return dateTime.format(formatter);
    }
}
