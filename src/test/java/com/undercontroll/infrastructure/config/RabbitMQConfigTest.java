package com.undercontroll.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConnectionFactory mockCF = mock(ConnectionFactory.class);

    @Test
    void emailQueue_shouldSetDeadLetterExchange_toRetryExchange() {
        Queue queue = config.emailQueue();

        assertThat(queue.getArguments().get("x-dead-letter-exchange")).isEqualTo("notification.retry");
    }

    @Test
    void emailQueue_shouldSetDeadLetterRoutingKey() {
        Queue queue = config.emailQueue();

        assertThat(queue.getArguments().get("x-dead-letter-routing-key")).isEqualTo("email.retry");
    }

    @Test
    void emailRetryQueue_shouldSetMessageTtl_ofTenSeconds() {
        Queue queue = config.emailRetryQueue();

        assertThat(queue.getArguments().get("x-message-ttl")).isEqualTo(10000);
    }

    @Test
    void emailRetryQueue_shouldSetDeadLetterExchange_backToMainExchange() {
        Queue queue = config.emailRetryQueue();

        assertThat(queue.getArguments().get("x-dead-letter-exchange")).isEqualTo("notification.events");
    }

    @Test
    void emailDlq_shouldBeDurable() {
        Queue queue = config.emailDlq();

        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void notificationExchange_shouldHaveCorrectName() {
        TopicExchange exchange = config.notificationExchange();

        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.NOTIFICATION_EXCHANGE);
    }

    @Test
    void emailBinding_shouldUseAnnouncementWildcardRoutingKey() {
        Queue emailQueue = config.emailQueue();
        TopicExchange exchange = config.notificationExchange();
        Binding binding = config.emailBinding(emailQueue, exchange);

        assertThat(binding.getRoutingKey()).isEqualTo("announcement.*");
    }

    @Test
    void jackson2JsonMessageConverter_shouldNotBeNull() {
        Jackson2JsonMessageConverter converter = config.jackson2JsonMessageConverter(objectMapper);

        assertThat(converter).isNotNull();
    }
}
