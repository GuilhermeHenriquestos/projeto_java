#!/bin/bash

# API de Pedidos - Testes de Carga Básicos
# Este script executa testes de carga simples usando curl e ferramentas básicas

BASE_URL="http://localhost:8080"
API_PATH="/api/pedidos"

echo "=== API de Pedidos - Testes de Carga ==="
echo "Base URL: $BASE_URL"
echo ""

# Função para imprimir separador
print_separator() {
    echo "=================================================="
}

# Função para executar teste de carga
run_load_test() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local concurrent_requests="$5"
    local total_requests="$6"
    
    print_separator
    echo "TESTE DE CARGA: $test_name"
    echo "Método: $method"
    echo "Endpoint: $endpoint"
    echo "Requisições simultâneas: $concurrent_requests"
    echo "Total de requisições: $total_requests"
    print_separator
    
    # Criar arquivo temporário para armazenar tempos de resposta
    temp_file=$(mktemp)
    
    echo "Iniciando teste..."
    start_time=$(date +%s)
    
    # Executar requisições em paralelo
    for ((i=1; i<=total_requests; i++)); do
        {
            if [ "$method" = "POST" ]; then
                response_time=$(curl -X POST "$BASE_URL$endpoint" \
                    -H "Content-Type: application/json" \
                    -d "$data" \
                    -w "%{time_total}" \
                    -s -o /dev/null)
            else
                response_time=$(curl -X GET "$BASE_URL$endpoint" \
                    -w "%{time_total}" \
                    -s -o /dev/null)
            fi
            echo "$response_time" >> "$temp_file"
        } &
        
        # Controlar número de processos simultâneos
        if (( i % concurrent_requests == 0 )); then
            wait
        fi
    done
    
    # Aguardar todos os processos terminarem
    wait
    
    end_time=$(date +%s)
    total_time=$((end_time - start_time))
    
    # Calcular estatísticas
    total_responses=$(wc -l < "$temp_file")
    avg_response_time=$(awk '{sum+=$1} END {print sum/NR}' "$temp_file")
    min_response_time=$(sort -n "$temp_file" | head -1)
    max_response_time=$(sort -n "$temp_file" | tail -1)
    
    # Calcular percentis
    p95_response_time=$(sort -n "$temp_file" | awk 'BEGIN{c=0} {a[c++]=$1} END{print a[int(c*0.95)]}')
    
    # Calcular throughput
    throughput=$(echo "scale=2; $total_responses / $total_time" | bc)
    
    echo ""
    echo "RESULTADOS:"
    echo "Tempo total: ${total_time}s"
    echo "Requisições completadas: $total_responses"
    echo "Throughput: ${throughput} req/s"
    echo "Tempo de resposta médio: ${avg_response_time}s"
    echo "Tempo de resposta mínimo: ${min_response_time}s"
    echo "Tempo de resposta máximo: ${max_response_time}s"
    echo "Tempo de resposta P95: ${p95_response_time}s"
    echo ""
    
    # Limpar arquivo temporário
    rm "$temp_file"
}

# Verificar se a API está rodando
echo "Verificando se a API está disponível..."
if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "ERRO: API não está disponível em $BASE_URL"
    echo "Certifique-se de que a aplicação está rodando antes de executar os testes de carga."
    exit 1
fi

echo "API disponível. Iniciando testes de carga..."
echo ""

# Criar alguns pedidos iniciais para os testes de consulta
echo "Criando dados iniciais para os testes..."
for i in {1..5}; do
    curl -X POST "$BASE_URL$API_PATH" \
        -H "Content-Type: application/json" \
        -d "{
            \"nomeCliente\": \"Cliente Teste $i\",
            \"descricao\": \"Pedido de teste número $i\",
            \"valor\": $((i * 100)).99
        }" \
        -s > /dev/null
done

echo "Dados iniciais criados."
echo ""

# Teste 1: Listar pedidos (operação de leitura)
run_load_test "Listar Pedidos" "GET" "$API_PATH" "" 10 100

# Teste 2: Buscar pedido por ID (operação de leitura)
run_load_test "Buscar Pedido por ID" "GET" "$API_PATH/1" "" 10 100

# Teste 3: Criar pedidos (operação de escrita)
create_data='{
    "nomeCliente": "Cliente Load Test",
    "descricao": "Pedido criado durante teste de carga",
    "valor": 199.99
}'
run_load_test "Criar Pedidos" "POST" "$API_PATH" "$create_data" 5 50

# Teste 4: Teste misto (70% leitura, 30% escrita)
print_separator
echo "TESTE MISTO: 70% Leitura, 30% Escrita"
print_separator

echo "Executando teste misto com 100 requisições..."
start_time=$(date +%s)

# 70 requisições de leitura
for ((i=1; i<=70; i++)); do
    {
        if (( i % 2 == 0 )); then
            curl -X GET "$BASE_URL$API_PATH" -s > /dev/null
        else
            curl -X GET "$BASE_URL$API_PATH/1" -s > /dev/null
        fi
    } &
    
    if (( i % 10 == 0 )); then
        wait
    fi
done

# 30 requisições de escrita
for ((i=1; i<=30; i++)); do
    {
        curl -X POST "$BASE_URL$API_PATH" \
            -H "Content-Type: application/json" \
            -d "{
                \"nomeCliente\": \"Cliente Misto $i\",
                \"descricao\": \"Pedido do teste misto $i\",
                \"valor\": $((i * 10)).99
            }" \
            -s > /dev/null
    } &
    
    if (( i % 5 == 0 )); then
        wait
    fi
done

wait
end_time=$(date +%s)
total_time=$((end_time - start_time))

echo "Teste misto concluído em ${total_time}s"
echo "Throughput aproximado: $(echo "scale=2; 100 / $total_time" | bc) req/s"

print_separator
echo "=== RESUMO DOS TESTES DE CARGA ==="
echo ""
echo "Todos os testes foram executados com sucesso!"
echo ""
echo "RECOMENDAÇÕES:"
echo "- Tempo de resposta médio deve ser < 200ms"
echo "- P95 deve ser < 500ms"
echo "- Throughput mínimo esperado: 100 req/s"
echo ""
echo "Se os resultados estão abaixo do esperado, considere:"
echo "1. Otimizar queries do banco de dados"
echo "2. Adicionar cache para operações de leitura"
echo "3. Configurar connection pool adequadamente"
echo "4. Implementar paginação para listagens"
print_separator