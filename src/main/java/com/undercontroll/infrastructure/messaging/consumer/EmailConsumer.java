package com.undercontroll.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.undercontroll.domain.enums.EmailEventType;
import com.undercontroll.domain.events.AnnouncementCreatedEvent;
import com.undercontroll.domain.events.EmailEvent;
import com.undercontroll.domain.port.in.AnnouncementCreatedPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EmailConsumer {

    private final AnnouncementCreatedPort announcementCreatedPort;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification.email.queue")
    public void listen(EmailEvent event) {
        log.info("Received a event from {}, of type {}", event.service(), event.type());

        if (event.type().equals(EmailEventType.ANNOUNCEMENT_CREATED)) {
            AnnouncementCreatedEvent announcementEvent = objectMapper.convertValue(
                    event.data(), AnnouncementCreatedEvent.class
            );
            announcementCreatedPort.execute(announcementEvent);
        }
    }
}
