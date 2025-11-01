#!/bin/bash
# API de Pedidos - Script de Testes Manuais
# Execute este script para testar todos os endpoints da API

BASE_URL="http://localhost:8080"
API_PATH="/api/pedidos"

echo "=== API de Pedidos - Testes Manuais ==="
echo "Base URL: $BASE_URL"
echo ""

# Função para imprimir separador
print_separator() {
    echo "=================================================="
}

# Função para aguardar input do usuário
wait_for_input() {
    echo "Pressione Enter para continuar..."
    read
}

print_separator
echo "1. TESTE: Criar Pedido (Dados Válidos)"
print_separator
echo "Request:"
echo "POST $BASE_URL$API_PATH"
echo '{
  "nomeCliente": "João Silva",
  "descricao": "Pedido de produtos eletrônicos",
  "valor": 1299.99
}'
echo ""
echo "Response:"
curl -X POST "$BASE_URL$API_PATH" \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCliente": "João Silva",
    "descricao": "Pedido de produtos eletrônicos",
    "valor": 1299.99
  }' \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "2. TESTE: Criar Segundo Pedido"
print_separator
echo "Request:"
echo "POST $BASE_URL$API_PATH"
echo '{
  "nomeCliente": "Maria Santos",
  "descricao": "Pedido de livros técnicos",
  "valor": 89.90
}'
echo ""
echo "Response:"
curl -X POST "$BASE_URL$API_PATH" \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCliente": "Maria Santos",
    "descricao": "Pedido de livros técnicos",
    "valor": 89.90
  }' \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "3. TESTE: Listar Todos os Pedidos"
print_separator
echo "Request:"
echo "GET $BASE_URL$API_PATH"
echo ""
echo "Response:"
curl -X GET "$BASE_URL$API_PATH" \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "4. TESTE: Buscar Pedido por ID (ID = 1)"
print_separator
echo "Request:"
echo "GET $BASE_URL$API_PATH/1"
echo ""
echo "Response:"
curl -X GET "$BASE_URL$API_PATH/1" \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "5. TESTE: Buscar Pedido por ID Inexistente (ID = 999)"
print_separator
echo "Request:"
echo "GET $BASE_URL$API_PATH/999"
echo ""
echo "Response:"
curl -X GET "$BASE_URL$API_PATH/999" \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "6. TESTE: Criar Pedido com Dados Inválidos (Nome Vazio)"
print_separator
echo "Request:"
echo "POST $BASE_URL$API_PATH"
echo '{
  "nomeCliente": "",
  "descricao": "Descrição válida",
  "valor": 100.00
}'
echo ""
echo "Response:"
curl -X POST "$BASE_URL$API_PATH" \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCliente": "",
    "descricao": "Descrição válida",
    "valor": 100.00
  }' \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "7. TESTE: Criar Pedido com Valor Inválido (Valor = 0)"
print_separator
echo "Request:"
echo "POST $BASE_URL$API_PATH"
echo '{
  "nomeCliente": "Cliente Teste",
  "descricao": "Descrição válida",
  "valor": 0
}'
echo ""
echo "Response:"
curl -X POST "$BASE_URL$API_PATH" \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCliente": "Cliente Teste",
    "descricao": "Descrição válida",
    "valor": 0
  }' \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "8. TESTE: Buscar Pedido com ID Inválido (ID = abc)"
print_separator
echo "Request:"
echo "GET $BASE_URL$API_PATH/abc"
echo ""
echo "Response:"
curl -X GET "$BASE_URL$API_PATH/abc" \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "9. TESTE: Criar Pedido com Múltiplos Erros de Validação"
print_separator
echo "Request:"
echo "POST $BASE_URL$API_PATH"
echo '{
  "nomeCliente": "",
  "descricao": "",
  "valor": -10.50
}'
echo ""
echo "Response:"
curl -X POST "$BASE_URL$API_PATH" \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCliente": "",
    "descricao": "",
    "valor": -10.50
  }' \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

wait_for_input

print_separator
echo "10. TESTE: Verificar Lista Final de Pedidos"
print_separator
echo "Request:"
echo "GET $BASE_URL$API_PATH"
echo ""
echo "Response:"
curl -X GET "$BASE_URL$API_PATH" \
  -w "\nStatus Code: %{http_code}\n" \
  -s | jq '.'

print_separator
echo "=== TESTES CONCLUÍDOS ==="
echo "Verifique se todos os testes retornaram os resultados esperados:"
echo "- Testes 1, 2: Status 201 (Created)"
echo "- Testes 3, 4, 10: Status 200 (OK)"
echo "- Teste 5: Status 404 (Not Found)"
echo "- Testes 6, 7, 8, 9: Status 400 (Bad Request)"
print_separator