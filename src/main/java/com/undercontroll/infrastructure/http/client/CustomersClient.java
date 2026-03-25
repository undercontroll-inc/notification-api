package com.undercontroll.infrastructure.http.client;

import com.undercontroll.infrastructure.client.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "main-service", url = "${main-service.url}")
public interface CustomersClient {

    @GetMapping("/v1/api/users/customers/emails")
    List<UserDto> getCustomersThatHaveEmail(@RequestHeader("Authorization") String token);

}