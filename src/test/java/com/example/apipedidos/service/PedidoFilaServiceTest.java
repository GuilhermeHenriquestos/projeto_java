package com.example.apipedidos.service;

import com.example.apipedidos.dto.PedidoRequestDTO;
import com.example.apipedidos.dto.PedidoResponseDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a funcionalidade de fila de pedidos no PedidoService
 */
@ExtendWith(MockitoExtension.class)
class PedidoFilaServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private PedidoRequestDTO pedidoRequest;
    private Pedido pedidoEntity;

    @BeforeEach
    void setUp() {
        pedidoRequest = new PedidoRequestDTO();
        pedidoRequest.setNomeCliente("João Silva");
        pedidoRequest.setDescricao("Pedido de teste");
        pedidoRequest.setValor(new BigDecimal("100.00"));

        pedidoEntity = new Pedido();
        pedidoEntity.setId(1L);
        pedidoEntity.setNomeCliente("João Silva");
        pedidoEntity.setDescricao("Pedido de teste");
        pedidoEntity.setValor(new BigDecimal("100.00"));
        pedidoEntity.setDataPedido(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve adicionar pedido à fila após criação")
    void deveAdicionarPedidoNaFilaAposCriacao() {
        // Given
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoEntity);

        // When
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequest);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(pedidoService.getTamanhoDaFila()).isEqualTo(1);
        assertThat(pedidoService.isFilaVazia()).isFalse();
    }

    @Test
    @DisplayName("Deve processar pedido da fila em ordem LIFO")
    void deveProcessarPedidoEmOrdemLIFO() {
        // Given - Criar dois pedidos
        when(pedidoRepository.save(any(Pedido.class)))
                .thenReturn(pedidoEntity)
                .thenReturn(criarSegundoPedido());

        PedidoResponseDTO primeiro = pedidoService.criarPedido(pedidoRequest);
        
        PedidoRequestDTO segundoRequest = new PedidoRequestDTO();
        segundoRequest.setNomeCliente("Maria Santos");
        segundoRequest.setDescricao("Segundo pedido");
        segundoRequest.setValor(new BigDecimal("200.00"));
        
        PedidoResponseDTO segundo = pedidoService.criarPedido(segundoRequest);

        // When - Processar pedidos
        PedidoResponseDTO processado1 = pedidoService.processarProximoPedidoDaFila();
        PedidoResponseDTO processado2 = pedidoService.processarProximoPedidoDaFila();

        // Then - Deve processar em ordem LIFO (último criado, primeiro processado)
        assertThat(processado1.getId()).isEqualTo(segundo.getId());
        assertThat(processado2.getId()).isEqualTo(primeiro.getId());
        assertThat(pedidoService.isFilaVazia()).isTrue();
    }

    @Test
    @DisplayName("Deve visualizar próximo pedido sem remover da fila")
    void deveVisualizarProximoPedidoSemRemover() {
        // Given
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoEntity);
        PedidoResponseDTO criado = pedidoService.criarPedido(pedidoRequest);

        // When
        PedidoResponseDTO visualizado = pedidoService.visualizarProximoPedidoDaFila();

        // Then
        assertThat(visualizado).isNotNull();
        assertThat(visualizado.getId()).isEqualTo(criado.getId());
        assertThat(pedidoService.getTamanhoDaFila()).isEqualTo(1); // Não deve remover
        assertThat(pedidoService.isFilaVazia()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar null ao processar fila vazia")
    void deveRetornarNullAoProcessarFilaVazia() {
        // When
        PedidoResponseDTO resultado = pedidoService.processarProximoPedidoDaFila();

        // Then
        assertThat(resultado).isNull();
        assertThat(pedidoService.isFilaVazia()).isTrue();
        assertThat(pedidoService.getTamanhoDaFila()).isZero();
    }

    @Test
    @DisplayName("Deve retornar null ao visualizar fila vazia")
    void deveRetornarNullAoVisualizarFilaVazia() {
        // When
        PedidoResponseDTO resultado = pedidoService.visualizarProximoPedidoDaFila();

        // Then
        assertThat(resultado).isNull();
        assertThat(pedidoService.isFilaVazia()).isTrue();
    }

    private Pedido criarSegundoPedido() {
        Pedido segundo = new Pedido();
        segundo.setId(2L);
        segundo.setNomeCliente("Maria Santos");
        segundo.setDescricao("Segundo pedido");
        segundo.setValor(new BigDecimal("200.00"));
        segundo.setDataPedido(LocalDateTime.now());
        return segundo;
    }
}