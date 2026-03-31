package com.undercontroll.domain.events;

import java.time.LocalDateTime;

public record UserCreatedEvent(
        String name,
        String email,
        LocalDateTime createdAt
) {
}
