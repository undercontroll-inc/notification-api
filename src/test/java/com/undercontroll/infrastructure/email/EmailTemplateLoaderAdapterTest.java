package com.undercontroll.infrastructure.email;

import com.undercontroll.infrastructure.resource_loader.EmailTemplateLoaderAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTemplateLoaderAdapterTest {

    private final EmailTemplateLoaderAdapter loader = new EmailTemplateLoaderAdapter();

    @Test
    void load_shouldReturnTemplateContent_whenTemplateExists() {
        String content = loader.load("announcement_created.html");

        assertThat(content).isNotNull();
        assertThat(content).isNotBlank();
    }

    @Test
    void load_shouldThrowIllegalArgumentException_whenTemplateNotFound() {
        assertThatThrownBy(() -> loader.load("does_not_exist.html"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found");
    }
}
