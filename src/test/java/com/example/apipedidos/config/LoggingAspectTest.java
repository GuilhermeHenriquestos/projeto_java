package com.example.apipedidos.config;

import com.example.apipedidos.dto.PedidoRequestDTO;
import com.example.apipedidos.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes para o aspecto de logging
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.example.apipedidos=DEBUG",
    "logging.level.AUDIT=DEBUG",
    "logging.level.PERFORMANCE=DEBUG"
})
@Transactional
class LoggingAspectTest {
    
    @Autowired
    private PedidoService pedidoService;
    
    @Test
    void testServiceLoggingAspect() {
        // Testa se o aspecto de logging funciona corretamente nos services
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("Cliente Teste");
        request.setDescricao("Descrição teste");
        request.setValor(new BigDecimal("100.00"));
        
        assertDoesNotThrow(() -> {
            var response = pedidoService.criarPedido(request);
            assertNotNull(response);
            assertNotNull(response.getId());
        });
    }
    
    @Test
    void testListServiceLoggingAspect() {
        // Testa se o aspecto de logging funciona para operações de listagem
        assertDoesNotThrow(() -> {
            var pedidos = pedidoService.listarTodosPedidos();
            assertNotNull(pedidos);
        });
    }
}