package com.undercontroll.application.port;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

}
