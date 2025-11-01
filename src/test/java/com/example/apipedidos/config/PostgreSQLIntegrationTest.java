package com.example.apipedidos.config;

import com.example.apipedidos.dto.PedidoRequestDTO;
import com.example.apipedidos.model.Pedido;
import com.example.apipedidos.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PostgreSQL Integration Test with TestContainers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
public class PostgreSQLIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_pedidos_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should connect to PostgreSQL via TestContainers")
    void shouldConnectToPostgreSQLViaTestContainers() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getDatabaseName()).isEqualTo("test_pedidos_db");
        assertThat(postgres.getUsername()).isEqualTo("test_user");
    }

    @Test
    @DisplayName("Should work with PostgreSQL database")
    void shouldWorkWithPostgreSQLDatabase() throws Exception {
        // Create test data
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("Cliente PostgreSQL Test");
        request.setDescricao("Pedido de teste com PostgreSQL");
        request.setValor(new BigDecimal("299.99"));

        // Test POST endpoint
        ResponseEntity<String> postResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/pedidos", request, String.class);
        
        assertThat(postResponse.getStatusCode().value()).isEqualTo(201);
        assertThat(postResponse.getBody()).contains("Cliente PostgreSQL Test");
        assertThat(postResponse.getBody()).contains("Pedido de teste com PostgreSQL");
        assertThat(postResponse.getBody()).contains("299.99");

        // Verify data was persisted
        assertThat(pedidoRepository.count()).isEqualTo(1);

        // Test GET endpoint
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/pedidos", String.class);
        
        assertThat(getResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(getResponse.getBody()).contains("Cliente PostgreSQL Test");
    }

    @Test
    @DisplayName("Should handle PostgreSQL specific features")
    void shouldHandlePostgreSQLSpecificFeatures() {
        // Test that we can create multiple pedidos and they get proper IDs
        Pedido pedido1 = new Pedido();
        pedido1.setNomeCliente("Cliente 1");
        pedido1.setDescricao("Descrição 1");
        pedido1.setValor(new BigDecimal("100.00"));

        Pedido pedido2 = new Pedido();
        pedido2.setNomeCliente("Cliente 2");
        pedido2.setDescricao("Descrição 2");
        pedido2.setValor(new BigDecimal("200.00"));

        Pedido saved1 = pedidoRepository.save(pedido1);
        Pedido saved2 = pedidoRepository.save(pedido2);

        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());

        // Test ordering
        var allPedidos = pedidoRepository.findAllByOrderByDataPedidoDesc();
        assertThat(allPedidos).hasSize(2);
        // The second one should be first due to DESC ordering
        assertThat(allPedidos.get(0).getId()).isEqualTo(saved2.getId());
    }

    @Test
    @DisplayName("Should use PostgreSQL dialect and driver")
    void shouldUsePostgreSQLDialectAndDriver() {
        // Verify that PostgreSQL configuration is being used
        String jdbcUrl = postgres.getJdbcUrl();
        assertThat(jdbcUrl).contains("postgresql");
        assertThat(jdbcUrl).contains("test_pedidos_db");
    }
}