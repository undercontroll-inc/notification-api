package com.undercontroll.application.usecase;

import com.undercontroll.domain.events.UserCreatedEvent;

public interface UserCreatedPort {

    void execute(UserCreatedEvent event);

}
