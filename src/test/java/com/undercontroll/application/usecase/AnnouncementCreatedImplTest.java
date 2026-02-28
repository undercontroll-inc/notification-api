package com.undercontroll.application.usecase;

import com.undercontroll.application.service.EmailService;
import com.undercontroll.domain.events.AnnouncementCreatedEvent;
import com.undercontroll.domain.port.out.EmailTemplateLoader;
import com.undercontroll.infrastructure.client.MainServiceClient;
import com.undercontroll.infrastructure.client.UserDto;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementCreatedImplTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateLoader emailTemplateLoader;

    @Mock
    private MainServiceClient mainServiceClient;

    @InjectMocks
    private AnnouncementCreatedImpl useCase;

    // Flat template so buildTemplate() can be exercised directly
    private static final String FLAT_TEMPLATE = "{{type}}|{{title}}|{{content}}|{{createdAt}}|{{year}}";

    @BeforeEach
    void setUp() {
        // lenient: recover() test doesn't call execute() so this stub would otherwise be flagged
        lenient().when(emailTemplateLoader.load("announcement_created.html")).thenReturn(FLAT_TEMPLATE);
    }

    private UserDto makeUser(String email) {
        return new UserDto(1, "Name", email, "Last", null, null, null, null, null, false, false, false, "CUSTOMER");
    }

    @Test
    void execute_shouldSendEmailToEachUser_whenMultipleUsersExist() {
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "Title", "Content", "INFO", LocalDateTime.now(), "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString()))
                .thenReturn(List.of(makeUser("a@test.com"), makeUser("b@test.com"), makeUser("c@test.com")));

        useCase.execute(event);

        verify(emailService, times(3)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void execute_shouldUseEventTitleInEmailSubject() {
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "My Title", "Content", "INFO", LocalDateTime.now(), "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(List.of(makeUser("user@test.com")));

        useCase.execute(event);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), subjectCaptor.capture(), anyString());
        assertThat(subjectCaptor.getValue()).isEqualTo("Novo Aviso: My Title");
    }

    @Test
    void execute_shouldNotSendAnyEmail_whenUserListIsNull() {
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "Title", "Content", "INFO", LocalDateTime.now(), "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(null);

        useCase.execute(event);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void execute_shouldNotSendAnyEmail_whenUserListIsEmpty() {
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "Title", "Content", "INFO", LocalDateTime.now(), "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(List.of());

        useCase.execute(event);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void execute_shouldContinueSendingToRemainingUsers_whenOneEmailFails() {
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "Title", "Content", "INFO", LocalDateTime.now(), "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString()))
                .thenReturn(List.of(makeUser("fail@test.com"), makeUser("ok@test.com")));
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(eq("fail@test.com"), anyString(), anyString());

        useCase.execute(event);

        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
        verify(emailService).sendEmail(eq("ok@test.com"), anyString(), anyString());
    }

    @Test
    void execute_shouldReplaceAllPlaceholders_inTemplate() {
        LocalDateTime publishedAt = LocalDateTime.of(2024, 3, 15, 10, 30);
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "My Title", "My Content", "UPDATES", publishedAt, "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(List.of(makeUser("user@test.com")));

        useCase.execute(event);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
        String body = bodyCaptor.getValue();
        assertThat(body).contains("UPDATES");
        assertThat(body).contains("My Title");
        assertThat(body).contains("My Content");
        assertThat(body).contains("15/03/2024 às 10:30");
    }

    @Test
    void execute_shouldReplaceNullEventFields_withEmptyStrings() {
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, null, null, null, LocalDateTime.now(), "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(List.of(makeUser("user@test.com")));

        useCase.execute(event);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).doesNotContain("null");
    }

    @Test
    void execute_shouldFormatPublishedAt_asPortugueseDateTime() {
        LocalDateTime publishedAt = LocalDateTime.of(2024, 3, 15, 10, 30);
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "Title", "Content", "INFO", publishedAt, "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(List.of(makeUser("user@test.com")));

        useCase.execute(event);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("15/03/2024 às 10:30");
    }

    @Test
    void execute_shouldUseEmptyString_whenPublishedAtIsNull() {
        // Template: "{{type}}|{{title}}|{{content}}|{{createdAt}}|{{year}}"
        // With null publishedAt, {{createdAt}} -> "" producing "||" between content and year
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "Title", "Content", "INFO", null, "token");
        when(mainServiceClient.getCustomersThatHaveEmail(anyString())).thenReturn(List.of(makeUser("user@test.com")));

        useCase.execute(event);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("||");
    }

    @Test
    void recover_shouldCompleteWithoutThrowing() {
        FeignException mockEx = mock(FeignException.class);
        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(1, "Title", "Content", "INFO", LocalDateTime.now(), "token");

        assertThatCode(() -> useCase.recover(mockEx, event)).doesNotThrowAnyException();
    }
}
