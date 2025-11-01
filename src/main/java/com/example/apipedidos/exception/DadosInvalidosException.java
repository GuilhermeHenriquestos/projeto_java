package com.example.apipedidos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando dados inválidos são fornecidos para operações de negócio
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DadosInvalidosException extends RuntimeException {
    
    /**
     * Construtor que recebe uma mensagem de erro
     * @param message Mensagem descritiva do erro de validação
     */
    public DadosInvalidosException(String message) {
        super(message);
    }
    
    /**
     * Construtor que recebe uma mensagem e a causa do erro
     * @param message Mensagem descritiva do erro de validação
     * @param cause Causa raiz da exceção
     */
    public DadosInvalidosException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Construtor para validações específicas de campo
     * @param campo Nome do campo inválido
     * @param valor Valor inválido fornecido
     * @param motivo Motivo da invalidação
     */
    public DadosInvalidosException(String campo, Object valor, String motivo) {
        super(String.format("Dados inválidos para o campo '%s' com valor '%s': %s", campo, valor, motivo));
    }
}