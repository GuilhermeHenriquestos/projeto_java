-- Schema SQL para criação da tabela pedidos e índices
-- Este arquivo é usado para ambientes de produção onde ddl-auto=validate

-- Criação da tabela pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id BIGSERIAL PRIMARY KEY,
    nome_cliente VARCHAR(255) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    valor DECIMAL(10,2) NOT NULL CHECK (valor > 0),
    data_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criação de índices para otimização de consultas
CREATE INDEX IF NOT EXISTS idx_pedidos_data_pedido ON pedidos(data_pedido DESC);
CREATE INDEX IF NOT EXISTS idx_pedidos_nome_cliente ON pedidos(nome_cliente);
CREATE INDEX IF NOT EXISTS idx_pedidos_valor ON pedidos(valor);

-- Comentários para documentação
COMMENT ON TABLE pedidos IS 'Tabela para armazenamento de pedidos de clientes';
COMMENT ON COLUMN pedidos.id IS 'Identificador único do pedido';
COMMENT ON COLUMN pedidos.nome_cliente IS 'Nome do cliente que fez o pedido';
COMMENT ON COLUMN pedidos.descricao IS 'Descrição detalhada do pedido';
COMMENT ON COLUMN pedidos.valor IS 'Valor total do pedido em formato decimal';
COMMENT ON COLUMN pedidos.data_pedido IS 'Data e hora de criação do pedido';