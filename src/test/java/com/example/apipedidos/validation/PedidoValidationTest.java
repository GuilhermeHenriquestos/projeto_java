package com.example.apipedidos.validation;

import com.example.apipedidos.dto.PedidoRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes específicos para validações Jakarta Validation
 * Foca em testar limites, formatos e mensagens de erro personalizadas
 */
@SpringBootTest
@ActiveProfiles("test")
class PedidoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== TESTES DE VALIDAÇÃO PARA NOME CLIENTE ==========

    @Test
    @DisplayName("Nome cliente - Deve aceitar nome com 255 caracteres (limite máximo)")
    void nomeCliente_Com255Caracteres_DeveSerValido() {
        // Arrange
        String nome255Chars = "a".repeat(255);
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente(nome255Chars);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Nome cliente - Deve rejeitar nome com 256 caracteres (excede limite)")
    void nomeCliente_Com256Caracteres_DeveSerInvalido() {
        // Arrange
        String nome256Chars = "a".repeat(256);
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente(nome256Chars);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nomeCliente");
        assertThat(violation.getMessage()).isEqualTo("Nome do cliente deve ter no máximo 255 caracteres");
    }

    @Test
    @DisplayName("Nome cliente - Deve rejeitar nome vazio com mensagem personalizada")
    void nomeCliente_Vazio_DeveRetornarMensagemPersonalizada() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("");

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nomeCliente");
        assertThat(violation.getMessage()).isEqualTo("Nome do cliente é obrigatório");
    }

    @Test
    @DisplayName("Nome cliente - Deve rejeitar nome nulo com mensagem personalizada")
    void nomeCliente_Nulo_DeveRetornarMensagemPersonalizada() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente(null);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nomeCliente");
        assertThat(violation.getMessage()).isEqualTo("Nome do cliente é obrigatório");
    }

    @Test
    @DisplayName("Nome cliente - Deve rejeitar nome com apenas espaços em branco")
    void nomeCliente_ApenasEspacos_DeveSerInvalido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("   ");

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nomeCliente");
        assertThat(violation.getMessage()).isEqualTo("Nome do cliente é obrigatório");
    }

    @Test
    @DisplayName("Nome cliente - Deve aceitar nome com caracteres especiais e acentos")
    void nomeCliente_ComCaracteresEspeciais_DeveSerValido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setNomeCliente("José da Silva-Santos Jr. & Cia. Ltda. (São Paulo)");

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    // ========== TESTES DE VALIDAÇÃO PARA DESCRIÇÃO ==========

    @Test
    @DisplayName("Descrição - Deve aceitar descrição com 500 caracteres (limite máximo)")
    void descricao_Com500Caracteres_DeveSerValida() {
        // Arrange
        String descricao500Chars = "a".repeat(500);
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao(descricao500Chars);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Descrição - Deve rejeitar descrição com 501 caracteres (excede limite)")
    void descricao_Com501Caracteres_DeveSerInvalida() {
        // Arrange
        String descricao501Chars = "a".repeat(501);
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao(descricao501Chars);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("descricao");
        assertThat(violation.getMessage()).isEqualTo("Descrição deve ter no máximo 500 caracteres");
    }

    @Test
    @DisplayName("Descrição - Deve rejeitar descrição vazia com mensagem personalizada")
    void descricao_Vazia_DeveRetornarMensagemPersonalizada() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao("");

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("descricao");
        assertThat(violation.getMessage()).isEqualTo("Descrição é obrigatória");
    }

    @Test
    @DisplayName("Descrição - Deve rejeitar descrição nula com mensagem personalizada")
    void descricao_Nula_DeveRetornarMensagemPersonalizada() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao(null);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("descricao");
        assertThat(violation.getMessage()).isEqualTo("Descrição é obrigatória");
    }

    @Test
    @DisplayName("Descrição - Deve aceitar descrição com quebras de linha e caracteres especiais")
    void descricao_ComQuebrasLinhaECaracteresEspeciais_DeveSerValida() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setDescricao("Pedido especial:\n- Item 1 (R$ 50,00)\n- Item 2 @ 25% desconto\n- Observações: entrega urgente!");

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    // ========== TESTES DE VALIDAÇÃO PARA VALOR ==========

    @Test
    @DisplayName("Valor - Deve aceitar valor mínimo válido (0.01)")
    void valor_MinimoValido_DeveSerAceito() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("0.01"));

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valor - Deve rejeitar valor zero com mensagem personalizada")
    void valor_Zero_DeveRetornarMensagemPersonalizada() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(BigDecimal.ZERO);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("valor");
        assertThat(violation.getMessage()).isEqualTo("Valor deve ser maior que zero");
    }

    @Test
    @DisplayName("Valor - Deve rejeitar valor negativo com mensagem personalizada")
    void valor_Negativo_DeveRetornarMensagemPersonalizada() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("-10.50"));

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("valor");
        assertThat(violation.getMessage()).isEqualTo("Valor deve ser maior que zero");
    }

    @Test
    @DisplayName("Valor - Deve rejeitar valor nulo com mensagem personalizada")
    void valor_Nulo_DeveRetornarMensagemPersonalizada() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(null);

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("valor");
        assertThat(violation.getMessage()).isEqualTo("Valor é obrigatório");
    }

    @Test
    @DisplayName("Valor - Deve aceitar valor com 2 casas decimais")
    void valor_ComDuasCasasDecimais_DeveSerValido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("999.99"));

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valor - Deve aceitar valor com 1 casa decimal")
    void valor_ComUmaCasaDecimal_DeveSerValido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("50.5"));

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valor - Deve aceitar valor inteiro (sem casas decimais)")
    void valor_Inteiro_DeveSerValido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("100"));

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valor - Deve rejeitar valor com mais de 2 casas decimais")
    void valor_ComMaisDeDuasCasasDecimais_DeveSerInvalido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("99.999"));

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("valor");
        assertThat(violation.getMessage()).isEqualTo("Valor deve ter no máximo 8 dígitos inteiros e 2 decimais");
    }

    @Test
    @DisplayName("Valor - Deve aceitar valor máximo com 8 dígitos inteiros")
    void valor_Com8DigitosInteiros_DeveSerValido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("99999999.99"));

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valor - Deve rejeitar valor com mais de 8 dígitos inteiros")
    void valor_ComMaisDe8DigitosInteiros_DeveSerInvalido() {
        // Arrange
        PedidoRequestDTO request = createValidRequest();
        request.setValor(new BigDecimal("999999999.99")); // 9 dígitos inteiros

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        ConstraintViolation<PedidoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("valor");
        assertThat(violation.getMessage()).isEqualTo("Valor deve ter no máximo 8 dígitos inteiros e 2 decimais");
    }

    // ========== TESTES DE VALIDAÇÃO MÚLTIPLA ==========

    @Test
    @DisplayName("Múltiplas validações - Deve retornar todas as violações quando todos os campos são inválidos")
    void multiplasValidacoes_TodosCamposInvalidos_DeveRetornarTodasViolacoes() {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente(""); // Vazio
        request.setDescricao(null); // Nulo
        request.setValor(BigDecimal.ZERO); // Zero

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(3);
        
        // Verificar se todas as propriedades têm violações
        assertThat(violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList())
                .containsExactlyInAnyOrder("nomeCliente", "descricao", "valor");
    }

    @Test
    @DisplayName("Múltiplas validações - Deve retornar violações específicas para limites excedidos")
    void multiplasValidacoes_LimitesExcedidos_DeveRetornarViolacoesEspecificas() {
        // Arrange
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setNomeCliente("a".repeat(256)); // Excede limite de 255
        request.setDescricao("b".repeat(501)); // Excede limite de 500
        request.setValor(new BigDecimal("999999999.999")); // Excede limites de dígitos

        // Act
        Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(3);
        
        // Verificar mensagens específicas
        assertThat(violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList())
                .containsExactlyInAnyOrder(
                    "Nome do cliente deve ter no máximo 255 caracteres",
                    "Descrição deve ter no máximo 500 caracteres",
                    "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais"
                );
    }

    // ========== TESTES DE CASOS EXTREMOS ==========

    @Test
    @DisplayName("Casos extremos - Deve aceitar valores decimais com diferentes formatos")
    void casosExtremos_ValoresDecimaisFormatosDiferentes_DeveSerValido() {
        // Arrange & Act & Assert
        String[] valoresValidos = {
            "0.01", "1.00", "10.5", "100", "999.99", 
            "1234.56", "99999999.00", "0.1", "5.50"
        };
        
        for (String valorStr : valoresValidos) {
            PedidoRequestDTO request = createValidRequest();
            request.setValor(new BigDecimal(valorStr));
            
            Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);
            
            assertThat(violations)
                .as("Valor %s deveria ser válido", valorStr)
                .isEmpty();
        }
    }

    @Test
    @DisplayName("Casos extremos - Deve rejeitar valores decimais inválidos")
    void casosExtremos_ValoresDecimaisInvalidos_DeveSerInvalido() {
        // Arrange & Act & Assert
        String[] valoresInvalidos = {
            "0.00", "-0.01", "-10.50", "999999999.99", "100.999"
        };
        
        for (String valorStr : valoresInvalidos) {
            PedidoRequestDTO request = createValidRequest();
            request.setValor(new BigDecimal(valorStr));
            
            Set<ConstraintViolation<PedidoRequestDTO>> violations = validator.validate(request);
            
            assertThat(violations)
                .as("Valor %s deveria ser inválido", valorStr)
                .isNotEmpty();
        }
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