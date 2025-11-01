package com.example.apipedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
     * DTO para retornar informações sobre o status da fila
     */
@Data
@NoArgsConstructor
public class FilaStatusDTO {

        private int tamanho;
        private boolean vazia;

        public FilaStatusDTO(int tamanho, boolean vazia) {
                this.tamanho = tamanho;
                this.vazia = vazia;
        }

}
