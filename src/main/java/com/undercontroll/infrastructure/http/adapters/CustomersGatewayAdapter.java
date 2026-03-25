package com.undercontroll.infrastructure.http.adapters;

import com.undercontroll.application.port.CustomersGateway;
import com.undercontroll.infrastructure.client.UserDto;
import com.undercontroll.infrastructure.http.client.CustomersClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomersGatewayAdapter implements CustomersGateway {

    private final CustomersClient customersClient;

    @Override
    public List<UserDto> getCustomersThatHaveEmail(String token) {
        try {
            List<UserDto> users = customersClient.getCustomersThatHaveEmail("Bearer " + token);

            if(users == null) {
                return List.of();
            }

            return users;
        } catch (Exception e) {
            log.error("Error while fething users: {}", e.getMessage());

            return List.of();
        }
    }
}
