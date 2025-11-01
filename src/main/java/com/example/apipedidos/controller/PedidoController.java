package com.example.apipedidos.controller;

import com.example.apipedidos.dto.FilaStatusDTO;
import com.example.apipedidos.dto.PedidoRequestDTO;
import com.example.apipedidos.dto.PedidoResponseDTO;
import com.example.apipedidos.service.PedidoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de pedidos
 * Fornece endpoints para criar, consultar e listar pedidos
 */
@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@Valid @RequestBody PedidoRequestDTO request) {
        log.info("Recebida requisição POST para criar pedido: {}", request.getNomeCliente());

        PedidoResponseDTO pedidoCriado = pedidoService.criarPedido(request);

        log.info("Pedido criado com sucesso. ID: {}", pedidoCriado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCriado);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos() {
        log.info("Recebida requisição GET para listar todos os pedidos");

        List<PedidoResponseDTO> pedidos = pedidoService.listarTodosPedidos();

        log.info("Retornando {} pedidos", pedidos.size());

        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPedidoPorId(
            @PathVariable @Min(value = 1, message = "ID deve ser maior que zero") Long id) {

        log.info("Recebida requisição GET para buscar pedido com ID: {}", id);

        PedidoResponseDTO pedido = pedidoService.buscarPedidoPorId(id);

        log.info("Pedido encontrado: {}", pedido.getId());

        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/fila/processar")
    public ResponseEntity<PedidoResponseDTO> processarProximoPedido() {
        log.info("Recebida requisição POST para processar próximo pedido da fila");

        PedidoResponseDTO pedido = pedidoService.processarProximoPedidoDaFila();

        if (pedido == null) {
            log.info("Fila de pedidos está vazia");
            return ResponseEntity.noContent().build();
        }

        log.info("Pedido processado da fila: ID {}", pedido.getId());
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/fila/proximo")
    public ResponseEntity<PedidoResponseDTO> visualizarProximoPedido() {
        log.info("Recebida requisição GET para visualizar próximo pedido da fila");

        PedidoResponseDTO pedido = pedidoService.visualizarProximoPedidoDaFila();

        if (pedido == null) {
            log.info("Fila de pedidos está vazia");
            return ResponseEntity.noContent().build();
        }

        log.info("Próximo pedido da fila: ID {}", pedido.getId());
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/fila/status")
    public ResponseEntity<FilaStatusDTO> obterStatusDaFila() {
        log.info("Recebida requisição GET para obter status da fila");

        int tamanho = pedidoService.getTamanhoDaFila();
        boolean vazia = pedidoService.isFilaVazia();

        FilaStatusDTO status = new FilaStatusDTO(tamanho, vazia);

        log.info("Status da fila - Tamanho: {}, Vazia: {}", tamanho, vazia);
        return ResponseEntity.ok(status);
    }

    /**
     * Endpoint bônus: listar todos os pedidos da nova fila Queue
     */
    @GetMapping("/fila/mensagens")
    public ResponseEntity<List<PedidoResponseDTO>> listarMensagensDaFila() {
        log.info("Recebida requisição GET para listar mensagens da fila (Queue)");
        List<PedidoResponseDTO> mensagens = pedidoService.obterStatusFilaQueue();
        log.info("Retornando {} mensagens da fila", mensagens.size());
        return ResponseEntity.ok(mensagens);
    }
}
