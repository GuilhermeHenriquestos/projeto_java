-- Schema específico para PostgreSQL
-- Usado em ambiente de produção

-- Criação da sequência para IDs (se não existir)
CREATE SEQUENCE IF NOT EXISTS pedidos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Criação da tabela pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT DEFAULT nextval('pedidos_id_seq'::regclass) NOT NULL,
    nome_cliente VARCHAR(255) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    data_pedido TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pedidos_pkey PRIMARY KEY (id),
    CONSTRAINT pedidos_valor_check CHECK (valor > 0::numeric)
);

-- Criação de índices otimizados para PostgreSQL
CREATE INDEX IF NOT EXISTS idx_pedidos_data_pedido_desc ON pedidos USING btree (data_pedido DESC);
CREATE INDEX IF NOT EXISTS idx_pedidos_nome_cliente_gin ON pedidos USING gin (to_tsvector('portuguese'::regconfig, nome_cliente));
CREATE INDEX IF NOT EXISTS idx_pedidos_valor_btree ON pedidos USING btree (valor);

-- Configuração da sequência
ALTER SEQUENCE pedidos_id_seq OWNED BY pedidos.id;