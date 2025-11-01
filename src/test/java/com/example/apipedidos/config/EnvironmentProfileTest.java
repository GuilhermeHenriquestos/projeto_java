package com.example.apipedidos.config;

import com.example.apipedidos.repository.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Environment Profile Tests")
public class EnvironmentProfileTest {

    @Nested
    @DisplayName("Development Profile Tests")
    @SpringBootTest
    @ActiveProfiles("dev")
    @Transactional
    class DevProfileTest {

        @Autowired
        private Environment environment;

        @Autowired
        private DataSource dataSource;

        @Autowired
        private PedidoRepository pedidoRepository;

        @Test
        @DisplayName("Should load dev profile configuration correctly")
        void shouldLoadDevProfileConfiguration() {
            // Verify active profile
            String[] activeProfiles = environment.getActiveProfiles();
            assertThat(activeProfiles).contains("dev");

            // Verify H2 database configuration
            String datasourceUrl = environment.getProperty("spring.datasource.url");
            assertThat(datasourceUrl).contains("h2:mem");

            // Verify H2 console is enabled
            String h2ConsoleEnabled = environment.getProperty("spring.h2.console.enabled");
            assertThat(h2ConsoleEnabled).isEqualTo("true");

            // Verify DDL auto is create-drop for dev
            String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
            assertThat(ddlAuto).isEqualTo("create-drop");

            // Verify show-sql is enabled for dev
            String showSql = environment.getProperty("spring.jpa.show-sql");
            assertThat(showSql).isEqualTo("true");
        }

        @Test
        @DisplayName("Should have debug logging enabled in dev profile")
        void shouldHaveDebugLoggingInDevProfile() {
            String logLevel = environment.getProperty("logging.level.com.example.apipedidos");
            assertThat(logLevel).isEqualTo("DEBUG");

            String sqlLogLevel = environment.getProperty("logging.level.org.hibernate.SQL");
            assertThat(sqlLogLevel).isEqualTo("DEBUG");
        }

        @Test
        @DisplayName("Should expose additional actuator endpoints in dev")
        void shouldExposeAdditionalActuatorEndpointsInDev() {
            String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
            assertThat(exposedEndpoints).contains("metrics", "env", "configprops");
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
        @DisplayName("Should have repository working with H2")
        void shouldHaveRepositoryWorkingWithH2() {
            // Test that repository is working
            long initialCount = pedidoRepository.count();
            assertThat(initialCount).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Production Profile Tests")
    @SpringBootTest
    @ActiveProfiles("prod")
    @TestPropertySource(properties = {
            "spring.datasource.url=jdbc:h2:mem:prodtest;DB_CLOSE_DELAY=-1",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "DB_PASSWORD=test-password"
    })
    @Transactional
    class ProdProfileTest {

        @Autowired
        private Environment environment;

        @Autowired
        private DataSource dataSource;

        @Autowired
        private PedidoRepository pedidoRepository;

        @Test
        @DisplayName("Should load production profile configuration")
        void shouldLoadProductionProfileConfiguration() {
            // Verify active profile
            String[] activeProfiles = environment.getActiveProfiles();
            assertThat(activeProfiles).contains("prod");

            // Verify that production properties are loaded
            String logLevel = environment.getProperty("logging.level.org.springframework.web");
            assertThat(logLevel).isEqualTo("WARN");
        }

        @Test
        @DisplayName("Should resolve environment variables in prod profile")
        void shouldResolveEnvironmentVariablesInProdProfile() {
            // Test that environment variables are being resolved
            String dbPassword = environment.getProperty("DB_PASSWORD");
            assertThat(dbPassword).isEqualTo("test-password");
        }

        @Test
        @DisplayName("Should have production logging configuration")
        void shouldHaveProductionLoggingConfiguration() {
            // Verify production logging levels
            String webLogLevel = environment.getProperty("logging.level.org.springframework.web");
            assertThat(webLogLevel).isEqualTo("WARN");

            String sqlLogLevel = environment.getProperty("logging.level.org.hibernate.SQL");
            assertThat(sqlLogLevel).isEqualTo("WARN");
        }

        @Test
        @DisplayName("Should have limited actuator endpoints in prod")
        void shouldHaveLimitedActuatorEndpointsInProd() {
            String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
            assertThat(exposedEndpoints).isEqualTo("health,info,metrics");
            assertThat(exposedEndpoints).doesNotContain("env", "configprops");
        }

        @Test
        @DisplayName("Should have file logging configured in prod")
        void shouldHaveFileLoggingConfiguredInProd() {
            String logFileName = environment.getProperty("logging.file.name");
            assertThat(logFileName).contains("api-pedidos.log");
        }

        @Test
        @DisplayName("Should have repository working in prod profile")
        void shouldHaveRepositoryWorkingInProdProfile() {
            // Test that repository is working
            long initialCount = pedidoRepository.count();
            assertThat(initialCount).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Environment Variable Tests")
    @SpringBootTest
    @TestPropertySource(properties = {
            "DB_HOST=test-host",
            "DB_PORT=5433",
            "DB_NAME=test-db",
            "DB_USERNAME=test-user",
            "DB_PASSWORD=test-pass",
            "LOG_LEVEL=WARN",
            "SERVER_PORT=9090"
    })
    class EnvironmentVariableTest {

        @Autowired
        private Environment environment;

        @Test
        @DisplayName("Should resolve environment variables correctly")
        void shouldResolveEnvironmentVariablesCorrectly() {
            // Test database environment variables
            String dbHost = environment.getProperty("DB_HOST");
            assertThat(dbHost).isEqualTo("test-host");

            String dbPort = environment.getProperty("DB_PORT");
            assertThat(dbPort).isEqualTo("5433");

            String dbName = environment.getProperty("DB_NAME");
            assertThat(dbName).isEqualTo("test-db");

            String dbUsername = environment.getProperty("DB_USERNAME");
            assertThat(dbUsername).isEqualTo("test-user");

            String dbPassword = environment.getProperty("DB_PASSWORD");
            assertThat(dbPassword).isEqualTo("test-pass");

            // Test logging environment variable
            String logLevel = environment.getProperty("LOG_LEVEL");
            assertThat(logLevel).isEqualTo("WARN");

            // Test server port environment variable
            String serverPort = environment.getProperty("SERVER_PORT");
            assertThat(serverPort).isEqualTo("9090");
        }

        @Test
        @DisplayName("Should have environment variables available for configuration")
        void shouldHaveEnvironmentVariablesAvailableForConfiguration() {
            // Test that environment variables can be used in configuration
            // This test verifies that the variables are properly set and available
            assertThat(environment.getProperty("DB_HOST")).isNotNull();
            assertThat(environment.getProperty("DB_PORT")).isNotNull();
            assertThat(environment.getProperty("DB_NAME")).isNotNull();
            assertThat(environment.getProperty("DB_USERNAME")).isNotNull();
            assertThat(environment.getProperty("DB_PASSWORD")).isNotNull();
        }
    }
}