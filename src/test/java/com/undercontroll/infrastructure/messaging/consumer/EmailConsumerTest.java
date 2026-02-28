package com.undercontroll.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.undercontroll.domain.enums.EmailEventType;
import com.undercontroll.domain.events.AnnouncementCreatedEvent;
import com.undercontroll.domain.events.EmailEvent;
import com.undercontroll.domain.port.in.AnnouncementCreatedPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @Mock
    private AnnouncementCreatedPort announcementCreatedPort;

    // Real ObjectMapper with JavaTimeModule so convertValue() actually works
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @InjectMocks
    private EmailConsumer consumer;

    @Test
    void listen_shouldExecuteAnnouncementCreatedPort_whenEventTypeMatches() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 15, 10, 30);
        AnnouncementCreatedEvent data = new AnnouncementCreatedEvent(42, "Test Title", "Test Content", "UPDATES", now, "bearer-token");
        EmailEvent event = new EmailEvent("main-service", EmailEventType.ANNOUNCEMENT_CREATED, data, now);

        consumer.listen(event);

        verify(announcementCreatedPort, times(1)).execute(any(AnnouncementCreatedEvent.class));
    }

    @Test
    void listen_shouldPassAllFields_toPortExecution() {
        LocalDateTime publishedAt = LocalDateTime.of(2024, 3, 15, 10, 30);
        AnnouncementCreatedEvent data = new AnnouncementCreatedEvent(42, "Test Title", "Test Content", "UPDATES", publishedAt, "my-token");
        EmailEvent event = new EmailEvent("main-service", EmailEventType.ANNOUNCEMENT_CREATED, data, publishedAt);

        consumer.listen(event);

        ArgumentCaptor<AnnouncementCreatedEvent> captor = ArgumentCaptor.forClass(AnnouncementCreatedEvent.class);
        verify(announcementCreatedPort).execute(captor.capture());
        AnnouncementCreatedEvent captured = captor.getValue();
        assertThat(captured.id()).isEqualTo(42);
        assertThat(captured.title()).isEqualTo("Test Title");
        assertThat(captured.content()).isEqualTo("Test Content");
        assertThat(captured.type()).isEqualTo("UPDATES");
        assertThat(captured.publishedAt()).isEqualTo(publishedAt);
        assertThat(captured.token()).isEqualTo("my-token");
    }

    @Test
    void listen_shouldNotExecutePort_whenEventTypeIsNull() {
        // event.type() is null → null.equals(...) throws NullPointerException
        EmailEvent event = new EmailEvent("main-service", null, null, LocalDateTime.now());

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(NullPointerException.class);

        verify(announcementCreatedPort, never()).execute(any());
    }
}
