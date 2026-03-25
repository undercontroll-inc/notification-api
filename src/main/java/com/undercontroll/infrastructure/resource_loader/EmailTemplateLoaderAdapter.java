package com.undercontroll.infrastructure.resource_loader;

import com.undercontroll.application.port.EmailTemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class EmailTemplateLoaderAdapter implements EmailTemplateLoader {

    @Override
    public String load(String name) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/" + name)){

            if (inputStream == null) {
                throw new IllegalArgumentException("Template not found: " + name);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading the template: {}", name, e);
            throw new RuntimeException("Error while loading the template", e);
        }
    }
}
