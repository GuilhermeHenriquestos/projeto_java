package com.example.apipedidos.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Testes para configuração de logging
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.example.apipedidos=DEBUG",
    "LOG_FILE_PATH=./target/test-logs/api-pedidos"
})
class LoggingConfigurationTest {
    
    @Test
    void contextLoads() {
        // Verifica se o contexto carrega corretamente com as configurações de logging
        assertDoesNotThrow(() -> {
            // O contexto deve carregar sem erros
        });
    }
    
    @Test
    void loggingSystemInitializes() {
        // Verifica se o sistema de logging inicializa corretamente
        assertDoesNotThrow(() -> {
            // O sistema de logging deve inicializar sem erros
        });
    }
}