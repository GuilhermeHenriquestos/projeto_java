package com.example.apipedidos.repository;

import com.example.apipedidos.model.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes de Integração - PedidoRepository")
class PedidoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Pedido pedido1;
    private Pedido pedido2;
    private Pedido pedido3;

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        pedidoRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Criar pedidos de teste
        pedido1 = new Pedido();
        pedido1.setNomeCliente("João Silva");
        pedido1.setDescricao("Pedido de teste 1");
        pedido1.setValor(new BigDecimal("100.50"));

        pedido2 = new Pedido();
        pedido2.setNomeCliente("Maria Santos");
        pedido2.setDescricao("Pedido de teste 2");
        pedido2.setValor(new BigDecimal("250.75"));

        pedido3 = new Pedido();
        pedido3.setNomeCliente("Pedro Oliveira");
        pedido3.setDescricao("Pedido de teste 3");
        pedido3.setValor(new BigDecimal("75.25"));
    }

    @Test
    @DisplayName("Deve salvar um pedido com sucesso")
    void deveSalvarPedidoComSucesso() {
        // When
        Pedido pedidoSalvo = pedidoRepository.save(pedido1);

        // Then
        assertThat(pedidoSalvo).isNotNull();
        assertThat(pedidoSalvo.getId()).isNotNull();
        assertThat(pedidoSalvo.getNomeCliente()).isEqualTo("João Silva");
        assertThat(pedidoSalvo.getDescricao()).isEqualTo("Pedido de teste 1");
        assertThat(pedidoSalvo.getValor()).isEqualTo(new BigDecimal("100.50"));
        assertThat(pedidoSalvo.getDataPedido()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar ID automaticamente ao salvar pedido")
    void deveGerarIdAutomaticamente() {
        // When
        Pedido pedidoSalvo = pedidoRepository.save(pedido1);

        // Then
        assertThat(pedidoSalvo.getId()).isNotNull();
        assertThat(pedidoSalvo.getId()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Deve gerar timestamp automaticamente ao salvar pedido")
    void deveGerarTimestampAutomaticamente() {
        // Given
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        // When
        Pedido pedidoSalvo = pedidoRepository.save(pedido1);

        // Then
        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);
        assertThat(pedidoSalvo.getDataPedido()).isNotNull();
        assertThat(pedidoSalvo.getDataPedido()).isAfter(antes);
        assertThat(pedidoSalvo.getDataPedido()).isBefore(depois);
    }

    @Test
    @DisplayName("Deve buscar pedido por ID existente")
    void deveBuscarPedidoPorIdExistente() {
        // Given
        Pedido pedidoSalvo = pedidoRepository.save(pedido1);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(pedidoSalvo.getId());

        // Then
        assertThat(pedidoEncontrado).isPresent();
        assertThat(pedidoEncontrado.get().getId()).isEqualTo(pedidoSalvo.getId());
        assertThat(pedidoEncontrado.get().getNomeCliente()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        // When
        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(999L);

        // Then
        assertThat(pedidoEncontrado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void deveListarTodosPedidos() {
        // Given
        pedidoRepository.save(pedido1);
        pedidoRepository.save(pedido2);
        pedidoRepository.save(pedido3);

        // When
        List<Pedido> pedidos = pedidoRepository.findAll();

        // Then
        assertThat(pedidos).hasSize(3);
        assertThat(pedidos).extracting(Pedido::getNomeCliente)
                .containsExactlyInAnyOrder("João Silva", "Maria Santos", "Pedro Oliveira");
    }

    @Test
    @DisplayName("Deve atualizar pedido existente")
    void deveAtualizarPedidoExistente() {
        // Given
        Pedido pedidoSalvo = pedidoRepository.save(pedido1);
        entityManager.flush();
        entityManager.clear();

        // When
        pedidoSalvo.setNomeCliente("João Silva Atualizado");
        pedidoSalvo.setValor(new BigDecimal("150.75"));
        Pedido pedidoAtualizado = pedidoRepository.save(pedidoSalvo);

        // Then
        assertThat(pedidoAtualizado.getId()).isEqualTo(pedidoSalvo.getId());
        assertThat(pedidoAtualizado.getNomeCliente()).isEqualTo("João Silva Atualizado");
        assertThat(pedidoAtualizado.getValor()).isEqualTo(new BigDecimal("150.75"));
    }

    @Test
    @DisplayName("Deve deletar pedido por ID")
    void deveDeletarPedidoPorId() {
        // Given
        Pedido pedidoSalvo = pedidoRepository.save(pedido1);
        Long id = pedidoSalvo.getId();

        // When
        pedidoRepository.deleteById(id);

        // Then
        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(id);
        assertThat(pedidoEncontrado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar pedidos ordenados por data decrescente")
    void deveListarPedidosOrdenadosPorDataDecrescente() throws InterruptedException {
        // Given - Salvar pedidos com pequeno intervalo para garantir ordem diferente
        Pedido primeiro = pedidoRepository.save(pedido1);
        Thread.sleep(10); // Pequeno delay para garantir timestamps diferentes
        
        Pedido segundo = pedidoRepository.save(pedido2);
        Thread.sleep(10);
        
        Pedido terceiro = pedidoRepository.save(pedido3);

        // When
        List<Pedido> pedidosOrdenados = pedidoRepository.findAllByOrderByDataPedidoDesc();

        // Then
        assertThat(pedidosOrdenados).hasSize(3);
        assertThat(pedidosOrdenados.get(0).getId()).isEqualTo(terceiro.getId()); // Mais recente primeiro
        assertThat(pedidosOrdenados.get(1).getId()).isEqualTo(segundo.getId());
        assertThat(pedidosOrdenados.get(2).getId()).isEqualTo(primeiro.getId()); // Mais antigo por último
        
        // Verificar que as datas estão em ordem decrescente
        for (int i = 0; i < pedidosOrdenados.size() - 1; i++) {
            LocalDateTime dataAtual = pedidosOrdenados.get(i).getDataPedido();
            LocalDateTime proximaData = pedidosOrdenados.get(i + 1).getDataPedido();
            assertThat(dataAtual).isAfterOrEqualTo(proximaData);
        }
    }

    @Test
    @DisplayName("Deve buscar pedidos por nome do cliente contendo texto")
    void deveBuscarPedidosPorNomeClienteContendo() {
        // Given
        pedidoRepository.save(pedido1); // João Silva
        pedidoRepository.save(pedido2); // Maria Santos
        pedidoRepository.save(pedido3); // Pedro Oliveira

        // When
        List<Pedido> pedidosEncontrados = pedidoRepository.findByNomeClienteContaining("Silva");

        // Then
        assertThat(pedidosEncontrados).hasSize(1);
        assertThat(pedidosEncontrados.get(0).getNomeCliente()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar pedidos por nome")
    void deveRetornarListaVaziaQuandoNaoEncontrarPedidosPorNome() {
        // Given
        pedidoRepository.save(pedido1);

        // When
        List<Pedido> pedidosEncontrados = pedidoRepository.findByNomeClienteContaining("Inexistente");

        // Then
        assertThat(pedidosEncontrados).isEmpty();
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar pedido com nome cliente nulo")
    void deveFalharAoTentarSalvarPedidoComNomeClienteNulo() {
        // Given
        pedido1.setNomeCliente(null);

        // When & Then
        assertThatThrownBy(() -> {
            pedidoRepository.save(pedido1);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar pedido com descrição nula")
    void deveFalharAoTentarSalvarPedidoComDescricaoNula() {
        // Given
        pedido1.setDescricao(null);

        // When & Then
        assertThatThrownBy(() -> {
            pedidoRepository.save(pedido1);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar pedido com valor nulo")
    void deveFalharAoTentarSalvarPedidoComValorNulo() {
        // Given
        pedido1.setValor(null);

        // When & Then
        assertThatThrownBy(() -> {
            pedidoRepository.save(pedido1);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Deve contar total de pedidos corretamente")
    void deveContarTotalDePedidosCorretamente() {
        // Given
        pedidoRepository.save(pedido1);
        pedidoRepository.save(pedido2);

        // When
        long total = pedidoRepository.count();

        // Then
        assertThat(total).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve verificar se pedido existe por ID")
    void deveVerificarSePedidoExistePorId() {
        // Given
        Pedido pedidoSalvo = pedidoRepository.save(pedido1);

        // When
        boolean existe = pedidoRepository.existsById(pedidoSalvo.getId());
        boolean naoExiste = pedidoRepository.existsById(999L);

        // Then
        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }
}