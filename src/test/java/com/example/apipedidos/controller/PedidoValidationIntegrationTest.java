package com.example.apipedidos.controller;

import com.example.apipedidos.dto.PedidoRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração específicos para validações Jakarta Validation
 * Foca em verificar mensagens de erro personalizadas e comportamento da API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PedidoValidationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ========== TESTES DE MENSAGENS DE ERRO PERSONALIZADAS ==========

    @Test
    @DisplayName("Validação - Deve retornar mensagem personalizada para nome cliente obrigatório")
    void validacao_NomeClienteObrigatorio_DeveRetornarMensagemPersonalizada() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente(null);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Nome do cliente é obrigatório")));
    }

    @Test
    @DisplayName("Validação - Deve retornar mensagem personalizada para descrição obrigatória")
    void validacao_DescricaoObrigatoria_DeveRetornarMensagemPersonalizada() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao("");

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Descrição é obrigatória")));
    }

    @Test
    @DisplayName("Validação - Deve retornar mensagem personalizada para valor obrigatório")
    void validacao_ValorObrigatorio_DeveRetornarMensagemPersonalizada() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(null);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Valor é obrigatório")));
    }

    @Test
    @DisplayName("Validação - Deve retornar mensagem personalizada para valor maior que zero")
    void validacao_ValorMaiorQueZero_DeveRetornarMensagemPersonalizada() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Valor deve ser maior que zero")));
    }

    @Test
    @DisplayName("Validação - Deve retornar mensagem personalizada para limite de caracteres do nome")
    void validacao_LimiteCaracteresNome_DeveRetornarMensagemPersonalizada() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("a".repeat(256)); // Excede limite de 255

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Nome do cliente deve ter no máximo 255 caracteres")));
    }

    @Test
    @DisplayName("Validação - Deve retornar mensagem personalizada para limite de caracteres da descrição")
    void validacao_LimiteCaracteresDescricao_DeveRetornarMensagemPersonalizada() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao("a".repeat(501)); // Excede limite de 500

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Descrição deve ter no máximo 500 caracteres")));
    }

    @Test
    @DisplayName("Validação - Deve retornar mensagem personalizada para formato de dígitos do valor")
    void validacao_FormatoDigitosValor_DeveRetornarMensagemPersonalizada() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("999999999.999")); // Excede limites de dígitos

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")));
    }

    // ========== TESTES DE LIMITES ESPECÍFICOS ==========

    @Test
    @DisplayName("Limites - Deve aceitar nome com exatamente 255 caracteres")
    void limites_NomeCom255Caracteres_DeveSerAceito() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("a".repeat(255));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCliente").value("a".repeat(255)));
    }

    @Test
    @DisplayName("Limites - Deve aceitar descrição com exatamente 500 caracteres")
    void limites_DescricaoCom500Caracteres_DeveSerAceita() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao("b".repeat(500));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("b".repeat(500)));
    }

    @Test
    @DisplayName("Limites - Deve aceitar valor com 8 dígitos inteiros e 2 decimais")
    void limites_ValorCom8DigitosInteirosE2Decimais_DeveSerAceito() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("99999999.99"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(99999999.99));
    }

    @Test
    @DisplayName("Limites - Deve aceitar valor mínimo válido (0.01)")
    void limites_ValorMinimoValido_DeveSerAceito() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("0.01"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(0.01));
    }

    // ========== TESTES DE FORMATOS DE VALOR DECIMAL ==========

    @Test
    @DisplayName("Formato decimal - Deve aceitar valor inteiro sem casas decimais")
    void formatoDecimal_ValorInteiro_DeveSerAceito() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("100"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(100.00));
    }

    @Test
    @DisplayName("Formato decimal - Deve aceitar valor com 1 casa decimal")
    void formatoDecimal_ValorComUmaCasaDecimal_DeveSerAceito() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("50.5"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(50.5));
    }

    @Test
    @DisplayName("Formato decimal - Deve aceitar valor com 2 casas decimais")
    void formatoDecimal_ValorComDuasCasasDecimais_DeveSerAceito() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("99.99"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(99.99));
    }

    @Test
    @DisplayName("Formato decimal - Deve rejeitar valor com 3 casas decimais")
    void formatoDecimal_ValorComTresCasasDecimais_DeveSerRejeitado() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("99.999"));

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")));
    }

    // ========== TESTES DE DADOS NULOS E VAZIOS ==========

    @Test
    @DisplayName("Dados nulos - Deve rejeitar todos os campos nulos")
    void dadosNulos_TodosCamposNulos_DeveSerRejeitado() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente(null);
        request.setDescricao(null);
        request.setValor(null);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", anyOf(
                    containsString("Nome do cliente é obrigatório"),
                    containsString("Descrição é obrigatória"),
                    containsString("Valor é obrigatório")
                )));
    }

    @Test
    @DisplayName("Dados vazios - Deve rejeitar strings vazias")
    void dadosVazios_StringsVazias_DeveSerRejeitado() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("");
        request.setDescricao("");

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", anyOf(
                    containsString("Nome do cliente é obrigatório"),
                    containsString("Descrição é obrigatória")
                )));
    }

    @Test
    @DisplayName("Dados com espaços - Deve rejeitar strings com apenas espaços em branco")
    void dadosComEspacos_ApenasEspacos_DeveSerRejeitado() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("   ");
        request.setDescricao("   ");

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", anyOf(
                    containsString("Nome do cliente é obrigatório"),
                    containsString("Descrição é obrigatória")
                )));
    }

    // ========== TESTES DE CARACTERES ESPECIAIS ==========

    @Test
    @DisplayName("Caracteres especiais - Deve aceitar nome com acentos e caracteres especiais")
    void caracteresEspeciais_NomeComAcentos_DeveSerAceito() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("José da Silva-Santos Jr. & Cia. Ltda. (São Paulo)");

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCliente").value("José da Silva-Santos Jr. & Cia. Ltda. (São Paulo)"));
    }

    @Test
    @DisplayName("Caracteres especiais - Deve aceitar descrição com quebras de linha e símbolos")
    void caracteresEspeciais_DescricaoComQuebrasLinha_DeveSerAceita() throws Exception {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao("Pedido especial:\n- Item 1 (R$ 50,00)\n- Item 2 @ 25% desconto\n- Observações: entrega urgente!");

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Pedido especial:\n- Item 1 (R$ 50,00)\n- Item 2 @ 25% desconto\n- Observações: entrega urgente!"));
    }

    // ========== TESTES DE ESTRUTURA DE RESPOSTA DE ERRO ==========

    @Test
    @DisplayName("Estrutura de erro - Deve retornar estrutura completa de erro para validação")
    void estruturaErro_ValidacaoCompleta_DeveRetornarEstruturaCompleta() throws Exception {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("");
        request.setDescricao("");
        request.setValor(BigDecimal.ZERO);

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

    // ========== MÉTODO AUXILIAR ==========

    /**
     * Cria um PedidoRequestDTO válido para usar como base nos testes
     */
    private PedidoRequestDTO createValidRequest() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("João Silva");
        request.setDescricao("Pedido de teste");
        request.setValor(new BigDecimal("99.99"));
        return request;
    }
}