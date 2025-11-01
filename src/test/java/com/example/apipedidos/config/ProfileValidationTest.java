package com.example.apipedidos.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Development Profile Validation")
public class ProfileValidationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Should load development profile with H2 database")
    void shouldLoadDevProfileWithH2Database() throws Exception {
        // Verify profile is active
        assertThat(environment.getActiveProfiles()).contains("dev");

        // Verify H2 database connection
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            assertThat(metaData.getDatabaseProductName()).isEqualTo("H2");
        }

        // Verify development-specific configurations
        assertThat(environment.getProperty("spring.h2.console.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("spring.jpa.show-sql")).isEqualTo("true");
        assertThat(environment.getProperty("logging.level.com.example.apipedidos")).isEqualTo("DEBUG");
    }

    @Test
    @DisplayName("Should have debug logging configuration in dev profile")
    void shouldHaveDebugLoggingConfigurationInDevProfile() {
        // Verify debug logging levels
        assertThat(environment.getProperty("logging.level.com.example.apipedidos")).isEqualTo("DEBUG");
        assertThat(environment.getProperty("logging.level.org.hibernate.SQL")).isEqualTo("DEBUG");
        assertThat(environment.getProperty("logging.level.org.springframework.web")).isEqualTo("DEBUG");
    }

    @Test
    @DisplayName("Should expose additional actuator endpoints in dev profile")
    void shouldExposeAdditionalActuatorEndpointsInDevProfile() {
        String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
        assertThat(exposedEndpoints).contains("health", "info", "metrics", "env", "configprops");
    }
}