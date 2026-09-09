package com.example.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class DevelopmentProfilePropertiesTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    void devProfileOverridesDefaultProperties() {
        assertThat(appProperties.getEnvironment()).isEqualTo("development");
        assertThat(appProperties.isFeaturesEnabled()).isTrue();
        assertThat(appProperties.getRequestTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(appProperties.getAllowedOrigins()).containsExactly("http://localhost:3000");
    }
}
