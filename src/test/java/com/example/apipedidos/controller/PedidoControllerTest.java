package com.example.apipedidos.controller;

import com.example.apipedidos.dto.PedidoRequestDTO;
import com.example.apipedidos.model.Pedido;
import com.example.apipedidos.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes end-to-end para PedidoController
 * Testa todos os endpoints REST com cenários de sucesso e erro
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PedidoControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        pedidoRepository.deleteAll();
    }

    // ========== TESTES POST /api/pedidos ==========

    @Test
    @DisplayName("POST /api/pedidos - Deve criar pedido com dados válidos e retornar 201")
    void criarPedido_ComDadosValidos_DeveRetornar201() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("João Silva");
        request.setDescricao("Pedido de teste");
        request.setValor(new BigDecimal("99.99"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$.descricao").value("Pedido de teste"))
                .andExpect(jsonPath("$.valor").value(99.99))
                .andExpect(jsonPath("$.dataPedido").exists());
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar pedido com nome cliente vazio e retornar 400")
    void criarPedido_ComNomeClienteVazio_DeveRetornar400() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("");
        request.setDescricao("Pedido de teste");
        request.setValor(new BigDecimal("99.99"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/pedidos"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar pedido com nome cliente nulo e retornar 400")
    void criarPedido_ComNomeClienteNulo_DeveRetornar400() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente(null);
        request.setDescricao("Pedido de teste");
        request.setValor(new BigDecimal("99.99"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar pedido com descrição vazia e retornar 400")
    void criarPedido_ComDescricaoVazia_DeveRetornar400() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("João Silva");
        request.setDescricao("");
        request.setValor(new BigDecimal("99.99"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar pedido com valor zero e retornar 400")
    void criarPedido_ComValorZero_DeveRetornar400() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("João Silva");
        request.setDescricao("Pedido de teste");
        request.setValor(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar pedido com valor negativo e retornar 400")
    void criarPedido_ComValorNegativo_DeveRetornar400() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("João Silva");
        request.setDescricao("Pedido de teste");
        request.setValor(new BigDecimal("-10.00"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar pedido com valor nulo e retornar 400")
    void criarPedido_ComValorNulo_DeveRetornar400() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("João Silva");
        request.setDescricao("Pedido de teste");
        request.setValor(null);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar pedido com nome muito longo e retornar 400")
    void criarPedido_ComNomeMuitoLongo_DeveRetornar400() throws Exception {
        // Arrange
        String nomeLongo = "a".repeat(256); // 256 caracteres, excede o limite de 255
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente(nomeLongo);
        request.setDescricao("Pedido de teste");
        request.setValor(new BigDecimal("99.99"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve rejeitar JSON malformado e retornar 400")
    void criarPedido_ComJsonMalformado_DeveRetornar400() throws Exception {
        // Arrange
        String jsonMalformado = "{\"nomeCliente\":\"João\",\"descricao\":\"teste\",\"valor\":}";

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isBadRequest());
    }

    // ========== TESTES GET /api/pedidos ==========

    @Test
    @DisplayName("GET /api/pedidos - Deve retornar lista vazia quando não há pedidos e status 200")
    void listarPedidos_SemPedidos_DeveRetornarListaVazia() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/pedidos - Deve retornar lista com pedidos ordenados por data (mais recentes primeiro)")
    void listarPedidos_ComPedidos_DeveRetornarListaOrdenada() throws Exception {
        // Arrange
        Pedido pedido1 = new Pedido();
        pedido1.setNomeCliente("Cliente 1");
        pedido1.setDescricao("Primeiro pedido");
        pedido1.setValor(new BigDecimal("50.00"));
        pedido1.setDataPedido(LocalDateTime.now().minusHours(2));
        pedidoRepository.save(pedido1);

        Pedido pedido2 = new Pedido();
        pedido2.setNomeCliente("Cliente 2");
        pedido2.setDescricao("Segundo pedido");
        pedido2.setValor(new BigDecimal("75.00"));
        pedido2.setDataPedido(LocalDateTime.now().minusHours(1));
        pedidoRepository.save(pedido2);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nomeCliente").value("Cliente 2")) // Mais recente primeiro
                .andExpect(jsonPath("$[0].valor").value(75.00))
                .andExpect(jsonPath("$[1].nomeCliente").value("Cliente 1"))
                .andExpect(jsonPath("$[1].valor").value(50.00));
    }

    @Test
    @DisplayName("GET /api/pedidos - Deve retornar todos os campos dos pedidos")
    void listarPedidos_DeveRetornarTodosCampos() throws Exception {
        // Arrange
        Pedido pedido = new Pedido();
        pedido.setNomeCliente("João Silva");
        pedido.setDescricao("Pedido completo");
        pedido.setValor(new BigDecimal("99.99"));
        pedido.setDataPedido(LocalDateTime.now());
        pedidoRepository.save(pedido);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$[0].descricao").value("Pedido completo"))
                .andExpect(jsonPath("$[0].valor").value(99.99))
                .andExpect(jsonPath("$[0].dataPedido").exists());
    }

    // ========== TESTES GET /api/pedidos/{id} ==========

    @Test
    @DisplayName("GET /api/pedidos/{id} - Deve retornar pedido existente com status 200")
    void buscarPedidoPorId_ComIdExistente_DeveRetornarPedido() throws Exception {
        // Arrange
        Pedido pedido = new Pedido();
        pedido.setNomeCliente("Maria Santos");
        pedido.setDescricao("Pedido específico");
        pedido.setValor(new BigDecimal("150.75"));
        pedido.setDataPedido(LocalDateTime.now());
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/{id}", pedidoSalvo.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(pedidoSalvo.getId()))
                .andExpect(jsonPath("$.nomeCliente").value("Maria Santos"))
                .andExpect(jsonPath("$.descricao").value("Pedido específico"))
                .andExpect(jsonPath("$.valor").value(150.75))
                .andExpect(jsonPath("$.dataPedido").exists());
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Deve retornar 404 para ID inexistente")
    void buscarPedidoPorId_ComIdInexistente_DeveRetornar404() throws Exception {
        // Arrange
        Long idInexistente = 999L;

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/{id}", idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("999")))
                .andExpect(jsonPath("$.path").value("/api/pedidos/999"));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Deve retornar 400 para ID inválido (zero)")
    void buscarPedidoPorId_ComIdZero_DeveRetornar400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/pedidos/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Deve retornar 400 para ID inválido (negativo)")
    void buscarPedidoPorId_ComIdNegativo_DeveRetornar400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/pedidos/{id}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Deve retornar 400 para ID não numérico")
    void buscarPedidoPorId_ComIdNaoNumerico_DeveRetornar400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/pedidos/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    // ========== TESTES DE INTEGRAÇÃO COMPLETA ==========

    @Test
    @DisplayName("Fluxo completo - Criar pedido e depois consultar por ID")
    void fluxoCompleto_CriarEConsultar_DeveRetornarDadosConsistentes() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("Ana Costa");
        request.setDescricao("Pedido para teste de integração");
        request.setValor(new BigDecimal("299.99"));

        // Act 1: Criar pedido
        String responseJson = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        Long pedidoId = objectMapper.readTree(responseJson).get("id").asLong();

        // Act 2: Consultar pedido criado
        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.nomeCliente").value("Ana Costa"))
                .andExpect(jsonPath("$.descricao").value("Pedido para teste de integração"))
                .andExpect(jsonPath("$.valor").value(299.99));

        // Act 3: Verificar na listagem
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(pedidoId))
                .andExpect(jsonPath("$[0].nomeCliente").value("Ana Costa"));
    }

    @Test
    @DisplayName("Teste de múltiplos pedidos - Verificar ordenação e integridade")
    void multiplePedidos_DeveManterOrdenacaoEIntegridade() throws Exception {
        // Arrange & Act: Criar múltiplos pedidos
        for (int i = 1; i <= 3; i++) {
            PedidoRequestDTO request = new PedidoRequestDTO();
            request.setNomeCliente("Cliente " + i);
            request.setDescricao("Descrição " + i);
            request.setValor(new BigDecimal(i * 10 + ".00"));

            mockMvc.perform(post("/api/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Pequena pausa para garantir diferença no timestamp
            Thread.sleep(10);
        }

        // Assert: Verificar listagem ordenada
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nomeCliente").value("Cliente 3")) // Mais recente
                .andExpect(jsonPath("$[1].nomeCliente").value("Cliente 2"))
                .andExpect(jsonPath("$[2].nomeCliente").value("Cliente 1")); // Mais antigo
    }
}