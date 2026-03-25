package com.undercontroll.application.usecase;

import com.undercontroll.domain.events.AnnouncementCreatedEvent;

public interface AnnouncementCreatedPort {

    void execute(
            AnnouncementCreatedEvent event
    );

}
