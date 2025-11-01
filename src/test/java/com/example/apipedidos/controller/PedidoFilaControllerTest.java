package com.example.apipedidos.controller;

import com.example.apipedidos.dto.PedidoResponseDTO;
import com.example.apipedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para os endpoints de fila de pedidos
 */
@WebMvcTest(PedidoController.class)
class PedidoFilaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    private PedidoResponseDTO pedidoResponse;

    @BeforeEach
    void setUp() {
        pedidoResponse = new PedidoResponseDTO();
        pedidoResponse.setId(1L);
        pedidoResponse.setNomeCliente("João Silva");
        pedidoResponse.setDescricao("Pedido de teste");
        pedidoResponse.setValor(new BigDecimal("100.00"));
        pedidoResponse.setDataPedido(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/pedidos/fila/processar - Deve processar pedido da fila e retornar 200")
    void processarProximoPedido_ComPedidoNaFila_DeveRetornar200() throws Exception {
        // Arrange
        when(pedidoService.processarProximoPedidoDaFila()).thenReturn(pedidoResponse);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos/fila/processar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$.descricao").value("Pedido de teste"))
                .andExpect(jsonPath("$.valor").value(100.00));
    }

    @Test
    @DisplayName("POST /api/pedidos/fila/processar - Deve retornar 204 quando fila vazia")
    void processarProximoPedido_ComFilaVazia_DeveRetornar204() throws Exception {
        // Arrange
        when(pedidoService.processarProximoPedidoDaFila()).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos/fila/processar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /api/pedidos/fila/proximo - Deve visualizar próximo pedido e retornar 200")
    void visualizarProximoPedido_ComPedidoNaFila_DeveRetornar200() throws Exception {
        // Arrange
        when(pedidoService.visualizarProximoPedidoDaFila()).thenReturn(pedidoResponse);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/fila/proximo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$.descricao").value("Pedido de teste"))
                .andExpect(jsonPath("$.valor").value(100.00));
    }

    @Test
    @DisplayName("GET /api/pedidos/fila/proximo - Deve retornar 204 quando fila vazia")
    void visualizarProximoPedido_ComFilaVazia_DeveRetornar204() throws Exception {
        // Arrange
        when(pedidoService.visualizarProximoPedidoDaFila()).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/fila/proximo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /api/pedidos/fila/status - Deve retornar status da fila com dados corretos")
    void obterStatusDaFila_DeveRetornarStatusCorreto() throws Exception {
        // Arrange
        when(pedidoService.getTamanhoDaFila()).thenReturn(3);
        when(pedidoService.isFilaVazia()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/fila/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tamanho").value(3))
                .andExpect(jsonPath("$.vazia").value(false));
    }

    @Test
    @DisplayName("GET /api/pedidos/fila/status - Deve retornar status de fila vazia")
    void obterStatusDaFila_ComFilaVazia_DeveRetornarStatusVazio() throws Exception {
        // Arrange
        when(pedidoService.getTamanhoDaFila()).thenReturn(0);
        when(pedidoService.isFilaVazia()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/fila/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tamanho").value(0))
                .andExpect(jsonPath("$.vazia").value(true));
    }
}