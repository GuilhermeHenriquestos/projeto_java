package com.example.apipedidos.service;

import com.example.apipedidos.dto.PedidoRequestDTO;
import com.example.apipedidos.dto.PedidoResponseDTO;
import com.example.apipedidos.exception.PedidoNotFoundException;
import com.example.apipedidos.model.Pedido;
import com.example.apipedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela lógica de negócio relacionada aos pedidos
 */
@Service
@Transactional
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    // Agora usamos Queue em vez de Stack
    private final Queue<PedidoResponseDTO> filaPedidosQueue = new LinkedList<>();

    /**
     * Cria um novo pedido no sistema
     * @param request Dados do pedido a ser criado
     * @return DTO com os dados do pedido criado
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        log.info("Criando novo pedido para cliente: {}", request.getNomeCliente());

        validarDadosPedido(request);

        Pedido pedido = convertToEntity(request);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        log.info("Pedido criado com sucesso. ID: {}", pedidoSalvo.getId());

        PedidoResponseDTO pedidoResponse = convertToResponseDTO(pedidoSalvo);

        // Adicionar na nova fila (Queue)
        adicionarPedidoNaFilaComQueue(pedidoResponse);

        return pedidoResponse;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodosPedidos() {
        log.info("Listando todos os pedidos");

        List<Pedido> pedidos = pedidoRepository.findAllByOrderByDataPedidoDesc();

        log.info("Encontrados {} pedidos", pedidos.size());

        return pedidos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long id) {
        log.info("Buscando pedido com ID: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pedido não encontrado com ID: {}", id);
                    return new PedidoNotFoundException(id);
                });

        return convertToResponseDTO(pedido);
    }

    private void validarDadosPedido(PedidoRequestDTO request) {
        log.debug("Validando dados do pedido para cliente: {}", request.getNomeCliente());

        if (request.getNomeCliente() != null) {
            request.setNomeCliente(request.getNomeCliente().trim());
        }

        if (request.getDescricao() != null) {
            request.setDescricao(request.getDescricao().trim());
        }
    }

    private Pedido convertToEntity(PedidoRequestDTO request) {
        Pedido pedido = new Pedido();
        pedido.setNomeCliente(request.getNomeCliente());
        pedido.setDescricao(request.getDescricao());
        pedido.setValor(request.getValor());
        return pedido;
    }

    private PedidoResponseDTO convertToResponseDTO(Pedido pedido) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(pedido.getId());
        response.setNomeCliente(pedido.getNomeCliente());
        response.setDescricao(pedido.getDescricao());
        response.setValor(pedido.getValor());
        response.setDataPedido(pedido.getDataPedido());
        return response;
    }

    /**
     * Novo método para adicionar pedido à fila utilizando Queue
     */
    private void adicionarPedidoNaFilaComQueue(PedidoResponseDTO pedido) {
        filaPedidosQueue.offer(pedido);
        log.info("Pedido ID {} adicionado à fila (Queue). Total de pedidos: {}",
                pedido.getId(), filaPedidosQueue.size());
    }

    /**
     * Remove e retorna o próximo pedido da fila (FIFO)
     */
    public PedidoResponseDTO processarProximoPedidoDaFila() {
        if (filaPedidosQueue.isEmpty()) {
            log.info("Fila de pedidos está vazia");
            return null;
        }

        PedidoResponseDTO pedido = filaPedidosQueue.poll();
        log.info("Pedido ID {} removido da fila. Restam: {}", pedido.getId(), filaPedidosQueue.size());
        return pedido;
    }

    /**
     * Retorna o próximo pedido da fila sem removê-lo
     */
    public PedidoResponseDTO visualizarProximoPedidoDaFila() {
        if (filaPedidosQueue.isEmpty()) {
            log.info("Fila de pedidos está vazia");
            return null;
        }

        PedidoResponseDTO pedido = filaPedidosQueue.peek();
        log.info("Próximo pedido da fila: ID {}", pedido.getId());
        return pedido;
    }

    /**
     * Retorna o tamanho atual da fila
     */
    public int getTamanhoDaFila() {
        return filaPedidosQueue.size();
    }

    /**
     * Verifica se a fila está vazia
     */
    public boolean isFilaVazia() {
        return filaPedidosQueue.isEmpty();
    }

    /**
     * Retorna todos os pedidos atualmente na fila
     */
    public List<PedidoResponseDTO> obterTodasAsMensagens() {
        log.info("Obtendo todos os pedidos da fila. Total: {}", filaPedidosQueue.size());
        return new ArrayList<>(filaPedidosQueue);
    }

    /**
     * Método adicional (bônus) para listar status completo da fila
     */
    public List<PedidoResponseDTO> obterStatusFilaQueue() {
        log.info("Obtendo status da fila (Queue). Total: {}", filaPedidosQueue.size());
        return new ArrayList<>(filaPedidosQueue);
    }
}
