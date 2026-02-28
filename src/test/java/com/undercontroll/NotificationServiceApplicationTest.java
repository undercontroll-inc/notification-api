package com.undercontroll;

import com.undercontroll.domain.events.AnnouncementCreatedEvent;
import com.undercontroll.domain.port.in.AnnouncementCreatedPort;
import com.undercontroll.infrastructure.client.MainServiceClient;
import com.undercontroll.infrastructure.client.UserDto;
import feign.FeignException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceApplicationTest {

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private MainServiceClient mainServiceClient;

    @Autowired
    private AnnouncementCreatedPort announcementCreatedPort;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    @Test
    void contextLoads() {
        // Verifies the Spring context starts without errors
    }

    @Test
    void shouldSendEmailToAllUsers_whenAnnouncementEventProcessed() {
        List<UserDto> users = List.of(
                new UserDto(1, "Alice", "alice@test.com", "Smith", null, null, null, null, null, false, false, false, "CUSTOMER"),
                new UserDto(2, "Bob", "bob@test.com", "Jones", null, null, null, null, null, false, false, false, "CUSTOMER")
        );
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(users);

        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(
                1, "New Feature", "Details here", "UPDATES", LocalDateTime.now(), "valid-token"
        );

        announcementCreatedPort.execute(event);

        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void announcementCreatedPort_shouldRetryOnFeignException_andSucceedOnThirdAttempt() {
        // @Retryable is active via AOP proxy — announcementCreatedPort is the proxied bean
        FeignException feignEx = mock(FeignException.class);
        List<UserDto> users = List.of(
                new UserDto(1, "Alice", "alice@test.com", "Smith", null, null, null, null, null, false, false, false, "CUSTOMER")
        );
        when(mainServiceClient.getCustomersThatHaveEmail(anyString()))
                .thenThrow(feignEx)
                .thenThrow(feignEx)
                .thenReturn(users);

        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(
                1, "Retry Test", "Content", "UPDATES", LocalDateTime.now(), "token"
        );

        announcementCreatedPort.execute(event);

        verify(mainServiceClient, times(3)).getCustomersThatHaveEmail(anyString());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
