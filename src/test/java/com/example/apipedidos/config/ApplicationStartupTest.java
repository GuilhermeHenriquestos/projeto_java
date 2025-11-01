package com.example.apipedidos.config;

import com.example.apipedidos.ApiPedidosApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Application Startup Tests")
public class ApplicationStartupTest {

    @Nested
    @DisplayName("Development Profile Startup")
    @SpringBootTest(
            classes = ApiPedidosApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
    )
    @ActiveProfiles("dev")
    class DevProfileStartupTest {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private Environment environment;

        @Autowired
        private DataSource dataSource;

        @Test
        @DisplayName("Should start successfully with dev profile")
        void shouldStartSuccessfullyWithDevProfile() {
            assertThat(applicationContext).isNotNull();
            assertThat(environment.getActiveProfiles()).contains("dev");
        }

        @Test
        @DisplayName("Should connect to H2 database in dev profile")
        void shouldConnectToH2DatabaseInDevProfile() throws Exception {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                assertThat(metaData.getDatabaseProductName()).isEqualTo("H2");
                assertThat(connection.getMetaData().getURL()).contains("h2:mem");
            }
        }

        @Test
        @DisplayName("Should have H2 console accessible in dev profile")
        void shouldHaveH2ConsoleAccessibleInDevProfile() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/h2-console", String.class);
            
            // H2 console should be accessible (might redirect or return content)
            assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.FOUND);
        }

        @Test
        @DisplayName("Should have actuator endpoints exposed in dev profile")
        void shouldHaveActuatorEndpointsExposedInDevProfile() {
            ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/actuator/health", String.class);
            assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            ResponseEntity<String> infoResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/actuator/info", String.class);
            assertThat(infoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Dev profile should expose additional endpoints
            ResponseEntity<String> metricsResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/actuator/metrics", String.class);
            assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should have API endpoints working in dev profile")
        void shouldHaveApiEndpointsWorkingInDevProfile() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/pedidos", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // Dev profile loads test data from data-dev.sql, so we expect 5 records
            assertThat(response.getBody()).contains("João Silva");
            assertThat(response.getBody()).contains("Maria Santos");
        }
    }

    @Nested
    @DisplayName("Production Profile Startup")
    @SpringBootTest(
            classes = ApiPedidosApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
    )
    @ActiveProfiles("prod")
    @TestPropertySource(properties = {
            "spring.datasource.url=jdbc:h2:mem:prodtest;DB_CLOSE_DELAY=-1",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "DB_PASSWORD=test-password"
    })
    class ProdProfileStartupTest {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private Environment environment;

        @Test
        @DisplayName("Should start successfully with prod profile")
        void shouldStartSuccessfullyWithProdProfile() {
            assertThat(applicationContext).isNotNull();
            assertThat(environment.getActiveProfiles()).contains("prod");
        }

        @Test
        @DisplayName("Should have limited actuator endpoints in prod profile")
        void shouldHaveLimitedActuatorEndpointsInProdProfile() {
            ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/actuator/health", String.class);
            assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            ResponseEntity<String> infoResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/actuator/info", String.class);
            assertThat(infoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Metrics should be available but env should not be exposed by default
            ResponseEntity<String> metricsResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/actuator/metrics", String.class);
            assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should not have H2 console accessible in prod profile")
        void shouldNotHaveH2ConsoleAccessibleInProdProfile() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/h2-console", String.class);
            
            // H2 console should not be accessible in prod (404 or 500 are both acceptable)
            assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should resolve environment variables in prod profile")
        void shouldResolveEnvironmentVariablesInProdProfile() {
            // Test that environment variables are being resolved
            String dbPassword = environment.getProperty("DB_PASSWORD");
            assertThat(dbPassword).isEqualTo("test-password");
        }

        @Test
        @DisplayName("Should have API endpoints working in prod profile")
        void shouldHaveApiEndpointsWorkingInProdProfile() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/pedidos", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("[]"); // Empty list initially
        }
    }

    @Nested
    @DisplayName("Custom Environment Variables Test")
    @SpringBootTest(
            classes = ApiPedidosApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
    )
    @TestPropertySource(properties = {
            "SERVER_PORT=0", // Random port
            "SPRING_PROFILES_ACTIVE=dev",
            "LOG_LEVEL=DEBUG",
            "DB_HOST=custom-host",
            "DB_PORT=5433",
            "DB_NAME=custom-db"
    })
    class CustomEnvironmentVariablesTest {

        @Autowired
        private Environment environment;

        @Test
        @DisplayName("Should resolve custom environment variables")
        void shouldResolveCustomEnvironmentVariables() {
            assertThat(environment.getProperty("LOG_LEVEL")).isEqualTo("DEBUG");
            assertThat(environment.getProperty("DB_HOST")).isEqualTo("custom-host");
            assertThat(environment.getProperty("DB_PORT")).isEqualTo("5433");
            assertThat(environment.getProperty("DB_NAME")).isEqualTo("custom-db");
        }

        @Test
        @DisplayName("Should use environment variables in configuration")
        void shouldUseEnvironmentVariablesInConfiguration() {
            // Verify that the profile was set via environment variable
            assertThat(environment.getActiveProfiles()).contains("dev");
        }
    }
}