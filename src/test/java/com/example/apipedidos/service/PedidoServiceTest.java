package com.example.apipedidos.service;

import com.example.apipedidos.dto.PedidoRequestDTO;
import com.example.apipedidos.dto.PedidoResponseDTO;
import com.example.apipedidos.exception.PedidoNotFoundException;
import com.example.apipedidos.model.Pedido;
import com.example.apipedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe PedidoService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService - Testes Unitários")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private PedidoRequestDTO pedidoRequestValido;
    private Pedido pedidoEntity;
    private Pedido pedidoEntitySalvo;

    @BeforeEach
    void setUp() {
        // Configurar dados de teste
        pedidoRequestValido = new PedidoRequestDTO();
        pedidoRequestValido.setNomeCliente("João Silva");
        pedidoRequestValido.setDescricao("Pedido de teste");
        pedidoRequestValido.setValor(new BigDecimal("99.99"));

        pedidoEntity = new Pedido();
        pedidoEntity.setNomeCliente("João Silva");
        pedidoEntity.setDescricao("Pedido de teste");
        pedidoEntity.setValor(new BigDecimal("99.99"));

        pedidoEntitySalvo = new Pedido();
        pedidoEntitySalvo.setId(1L);
        pedidoEntitySalvo.setNomeCliente("João Silva");
        pedidoEntitySalvo.setDescricao("Pedido de teste");
        pedidoEntitySalvo.setValor(new BigDecimal("99.99"));
        pedidoEntitySalvo.setDataPedido(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar pedido com dados válidos")
    void deveCriarPedidoComDadosValidos() {
        // Given
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoEntitySalvo);

        // When
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequestValido);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNomeCliente()).isEqualTo("João Silva");
        assertThat(resultado.getDescricao()).isEqualTo("Pedido de teste");
        assertThat(resultado.getValor()).isEqualTo(new BigDecimal("99.99"));
        assertThat(resultado.getDataPedido()).isNotNull();

        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve normalizar dados do pedido ao criar")
    void deveNormalizarDadosDoPedidoAoCriar() {
        // Given
        PedidoRequestDTO requestComEspacos = new PedidoRequestDTO();
        requestComEspacos.setNomeCliente("  João Silva  ");
        requestComEspacos.setDescricao("  Pedido de teste  ");
        requestComEspacos.setValor(new BigDecimal("99.99"));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoEntitySalvo);

        // When
        pedidoService.criarPedido(requestComEspacos);

        // Then
        assertThat(requestComEspacos.getNomeCliente()).isEqualTo("João Silva");
        assertThat(requestComEspacos.getDescricao()).isEqualTo("Pedido de teste");
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve listar todos os pedidos quando existem pedidos")
    void deveListarTodosPedidosQuandoExistemPedidos() {
        // Given
        Pedido pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setNomeCliente("João Silva");
        pedido1.setDescricao("Primeiro pedido");
        pedido1.setValor(new BigDecimal("99.99"));
        pedido1.setDataPedido(LocalDateTime.now().minusHours(1));

        Pedido pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setNomeCliente("Maria Santos");
        pedido2.setDescricao("Segundo pedido");
        pedido2.setValor(new BigDecimal("149.99"));
        pedido2.setDataPedido(LocalDateTime.now());

        List<Pedido> pedidos = Arrays.asList(pedido2, pedido1); // Ordenados por data desc

        when(pedidoRepository.findAllByOrderByDataPedidoDesc()).thenReturn(pedidos);

        // When
        List<PedidoResponseDTO> resultado = pedidoService.listarTodosPedidos();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(2L);
        assertThat(resultado.get(0).getNomeCliente()).isEqualTo("Maria Santos");
        assertThat(resultado.get(1).getId()).isEqualTo(1L);
        assertThat(resultado.get(1).getNomeCliente()).isEqualTo("João Silva");

        verify(pedidoRepository, times(1)).findAllByOrderByDataPedidoDesc();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem pedidos")
    void deveRetornarListaVaziaQuandoNaoExistemPedidos() {
        // Given
        when(pedidoRepository.findAllByOrderByDataPedidoDesc()).thenReturn(Collections.emptyList());

        // When
        List<PedidoResponseDTO> resultado = pedidoService.listarTodosPedidos();

        // Then
        assertThat(resultado).isEmpty();
        verify(pedidoRepository, times(1)).findAllByOrderByDataPedidoDesc();
    }

    @Test
    @DisplayName("Deve buscar pedido por ID quando pedido existe")
    void deveBuscarPedidoPorIdQuandoPedidoExiste() {
        // Given
        Long id = 1L;
        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedidoEntitySalvo));

        // When
        PedidoResponseDTO resultado = pedidoService.buscarPedidoPorId(id);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNomeCliente()).isEqualTo("João Silva");
        assertThat(resultado.getDescricao()).isEqualTo("Pedido de teste");
        assertThat(resultado.getValor()).isEqualTo(new BigDecimal("99.99"));
        assertThat(resultado.getDataPedido()).isNotNull();

        verify(pedidoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existe")
    void deveLancarExcecaoQuandoPedidoNaoExiste() {
        // Given
        Long id = 999L;
        when(pedidoRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pedidoService.buscarPedidoPorId(id))
                .isInstanceOf(PedidoNotFoundException.class)
                .hasMessage("Pedido não encontrado com ID: " + id);

        verify(pedidoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve converter RequestDTO para Entity corretamente")
    void deveConverterRequestDTOParaEntityCorretamente() {
        // Given
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            // Verificar se os dados foram convertidos corretamente
            assertThat(pedido.getNomeCliente()).isEqualTo("João Silva");
            assertThat(pedido.getDescricao()).isEqualTo("Pedido de teste");
            assertThat(pedido.getValor()).isEqualTo(new BigDecimal("99.99"));
            assertThat(pedido.getId()).isNull(); // ID deve ser null antes de salvar
            
            // Simular o comportamento do banco que define ID e data
            pedido.setId(1L);
            pedido.setDataPedido(LocalDateTime.now());
            return pedido;
        });

        // When
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequestValido);

        // Then
        assertThat(resultado).isNotNull();
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve converter Entity para ResponseDTO corretamente")
    void deveConverterEntityParaResponseDTOCorretamente() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntitySalvo));

        // When
        PedidoResponseDTO resultado = pedidoService.buscarPedidoPorId(1L);

        // Then
        assertThat(resultado.getId()).isEqualTo(pedidoEntitySalvo.getId());
        assertThat(resultado.getNomeCliente()).isEqualTo(pedidoEntitySalvo.getNomeCliente());
        assertThat(resultado.getDescricao()).isEqualTo(pedidoEntitySalvo.getDescricao());
        assertThat(resultado.getValor()).isEqualTo(pedidoEntitySalvo.getValor());
        assertThat(resultado.getDataPedido()).isEqualTo(pedidoEntitySalvo.getDataPedido());
    }

    @Test
    @DisplayName("Deve tratar dados nulos na normalização")
    void deveTratarDadosNulosNaNormalizacao() {
        // Given
        PedidoRequestDTO requestComNulos = new PedidoRequestDTO();
        requestComNulos.setNomeCliente(null);
        requestComNulos.setDescricao(null);
        requestComNulos.setValor(new BigDecimal("99.99"));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoEntitySalvo);

        // When & Then - Não deve lançar exceção
        assertThatCode(() -> pedidoService.criarPedido(requestComNulos))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve manter valores decimais precisos na conversão")
    void deveManterValoresDecimaisPrecisosNaConversao() {
        // Given
        BigDecimal valorPreciso = new BigDecimal("123.45");
        pedidoRequestValido.setValor(valorPreciso);
        
        pedidoEntitySalvo.setValor(valorPreciso);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoEntitySalvo);

        // When
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequestValido);

        // Then
        assertThat(resultado.getValor()).isEqualTo(valorPreciso);
        assertThat(resultado.getValor().scale()).isEqualTo(2);
    }
}