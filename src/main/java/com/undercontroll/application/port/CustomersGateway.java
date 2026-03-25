package com.undercontroll.application.port;

import com.undercontroll.infrastructure.client.UserDto;

import java.util.List;

public interface CustomersGateway {

    List<UserDto> getCustomersThatHaveEmail(String token);

}
