package com.example.apipedidos.repository;

import com.example.apipedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    /**
     * Busca todos os pedidos ordenados por data de pedido em ordem decrescente (mais recentes primeiro)
     * @return Lista de pedidos ordenada por data decrescente
     */
    List<Pedido> findAllByOrderByDataPedidoDesc();
    
    /**
     * Busca pedidos por nome do cliente contendo o texto especificado
     * @param nome Texto a ser buscado no nome do cliente
     * @return Lista de pedidos que contém o texto no nome do cliente
     */
    @Query("SELECT p FROM Pedido p WHERE p.nomeCliente LIKE %:nome%")
    List<Pedido> findByNomeClienteContaining(@Param("nome") String nome);
}