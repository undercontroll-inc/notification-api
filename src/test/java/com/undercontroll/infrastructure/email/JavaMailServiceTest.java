package com.undercontroll.infrastructure.email;

import com.undercontroll.domain.exception.MailSendingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaMailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private JavaMailService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "from", "sender@test.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    @Test
    void sendEmail_shouldCallMailSenderSend() {
        service.sendEmail("recipient@test.com", "Test Subject", "<p>Body</p>");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_shouldSetCorrectSubjectAndRecipient() throws Exception {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        service.sendEmail("recipient@test.com", "Test Subject", "<p>Body</p>");

        verify(mailSender).send(captor.capture());
        MimeMessage captured = captor.getValue();
        assertThat(captured.getSubject()).isEqualTo("Test Subject");
        assertThat(captured.getAllRecipients()[0].toString()).isEqualTo("recipient@test.com");
    }

    @Test
    void sendEmail_shouldThrowMailSendingException_whenMailSenderFails() {
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendEmail("recipient@test.com", "Subject", "<p>Body</p>"))
                .isInstanceOf(MailSendingException.class);
    }
}
