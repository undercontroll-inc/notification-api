package com.undercontroll.application.usecase;

import com.undercontroll.application.port.EmailService;
import com.undercontroll.application.port.EmailTemplateLoader;
import com.undercontroll.application.usecase.impl.UserCreatedImpl;
import com.undercontroll.domain.events.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreatedImplTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateLoader emailTemplateLoader;

    @InjectMocks
    private UserCreatedImpl useCase;

    private static final String TEMPLATE = "{{name}}|{{email}}|{{createdAt}}|{{year}}|{{websiteUrl}}|{{contactUrl}}";

    @BeforeEach
    void setUp() {
        lenient().when(emailTemplateLoader.load("user_created.html")).thenReturn(TEMPLATE);
    }

    @Test
    void execute_shouldSendWelcomeEmail_whenEmailExists() {
        UserCreatedEvent event = new UserCreatedEvent("Maria", "maria@teste.com", LocalDateTime.of(2026, 3, 30, 10, 30));

        useCase.execute(event);

        verify(emailService).sendEmail(eq("maria@teste.com"), contains("Bem-vindo"), anyString());
    }

    @Test
    void execute_shouldNotSendEmail_whenEventIsNull() {
        useCase.execute(null);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void execute_shouldNotSendEmail_whenEmailIsBlank() {
        UserCreatedEvent event = new UserCreatedEvent("Maria", "   ", LocalDateTime.now());

        useCase.execute(event);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void execute_shouldReplaceTemplateFields() {
        UserCreatedEvent event = new UserCreatedEvent("Maria", "maria@teste.com", LocalDateTime.of(2026, 3, 30, 10, 30));

        useCase.execute(event);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
        String body = bodyCaptor.getValue();

        assertThat(body).contains("Maria");
        assertThat(body).contains("maria@teste.com");
        assertThat(body).contains("30/03/2026 as 10:30");
        assertThat(body).contains("https://www.comercialirmaospelluci.com.br");
        assertThat(body).contains("mailto:comercialirmaopeluci@gmail.com");
    }
}
