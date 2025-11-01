package com.example.apipedidos.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o indicador de saúde do logging
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "LOG_FILE_PATH=./target/test-logs/api-pedidos"
})
class LoggingHealthIndicatorTest {
    
    @Autowired
    private LoggingHealthIndicator loggingHealthIndicator;
    
    @Test
    void testHealthIndicatorExists() {
        assertNotNull(loggingHealthIndicator);
    }
    
    @Test
    void testHealthCheck() {
        Health health = loggingHealthIndicator.health();
        
        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus());
        
        // Verifica se os detalhes estão presentes
        assertTrue(health.getDetails().containsKey("logsDirectory"));
        assertTrue(health.getDetails().containsKey("logsDirectoryWritable"));
        assertTrue(health.getDetails().containsKey("activeProfile"));
        assertTrue(health.getDetails().containsKey("logLevel"));
        assertTrue(health.getDetails().containsKey("aspectsEnabled"));
    }
}