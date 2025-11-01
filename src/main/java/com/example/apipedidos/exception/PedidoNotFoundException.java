package com.example.apipedidos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um pedido não é encontrado no sistema
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PedidoNotFoundException extends RuntimeException {
    
    /**
     * Construtor que recebe o ID do pedido não encontrado
     * @param id ID do pedido que não foi encontrado
     */
    public PedidoNotFoundException(Long id) {
        super("Pedido não encontrado com ID: " + id);
    }
    
    /**
     * Construtor que recebe uma mensagem personalizada
     * @param message Mensagem de erro personalizada
     */
    public PedidoNotFoundException(String message) {
        super(message);
    }
}