package com.undercontroll.infrastructure.email;

import com.undercontroll.application.port.EmailService;
import com.undercontroll.domain.exception.MailSendingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class JavaMailAdapter implements EmailService {


    private final JavaMailSender mailSender;

    private static final String from = "furquimmsw@gmail.com";

    @Override
    public void sendEmail(
            String to,
            String subject,
            String body
    ) {
        log.info("Sending email to {}, subject {}", to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);

            helper.setText(body, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new MailSendingException(
                    "Houve um erro ao enviar o email para %s: %s".formatted(to, e.getMessage())
            );
        }

    }
}
