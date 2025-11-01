#!/bin/bash

# API de Pedidos - Testes em Diferentes Ambientes
# Este script executa testes completos em diferentes perfis/ambientes

BASE_URL="http://localhost:8080"
API_PATH="/api/pedidos"

echo "=== API de Pedidos - Testes em Diferentes Ambientes ==="
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

# Função para verificar se a aplicação está rodando
check_app_running() {
    local port=$1
    local max_attempts=30
    local attempt=1
    
    echo "Verificando se a aplicação está disponível na porta $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "✅ Aplicação disponível na porta $port"
            return 0
        fi
        
        echo "Tentativa $attempt/$max_attempts - Aguardando aplicação..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "❌ Aplicação não está disponível na porta $port após $max_attempts tentativas"
    return 1
}

# Função para executar teste básico da API
test_api_basic() {
    local port=$1
    local profile=$2
    
    echo "Testando API no perfil $profile (porta $port)..."
    
    # Teste 1: Health check
    echo "1. Health Check:"
    health_response=$(curl -s "http://localhost:$port/actuator/health")
    if echo "$health_response" | grep -q "UP"; then
        echo "✅ Health check passou"
    else
        echo "❌ Health check falhou: $health_response"
        return 1
    fi
    
    # Teste 2: Listar pedidos (deve funcionar mesmo se vazio)
    echo "2. Listar pedidos:"
    list_response=$(curl -s -w "%{http_code}" "http://localhost:$port$API_PATH")
    http_code="${list_response: -3}"
    if [ "$http_code" = "200" ]; then
        echo "✅ Listagem de pedidos funcionando"
    else
        echo "❌ Listagem de pedidos falhou (HTTP $http_code)"
        return 1
    fi
    
    # Teste 3: Criar pedido
    echo "3. Criar pedido:"
    create_response=$(curl -s -w "%{http_code}" -X POST "http://localhost:$port$API_PATH" \
        -H "Content-Type: application/json" \
        -d '{
            "nomeCliente": "Cliente Teste '${profile}'",
            "descricao": "Pedido de teste no ambiente '${profile}'",
            "valor": 199.99
        }')
    http_code="${create_response: -3}"
    if [ "$http_code" = "201" ]; then
        echo "✅ Criação de pedido funcionando"
    else
        echo "❌ Criação de pedido falhou (HTTP $http_code)"
        echo "Response: ${create_response%???}"
        return 1
    fi
    
    # Teste 4: Buscar pedido por ID
    echo "4. Buscar pedido por ID:"
    get_response=$(curl -s -w "%{http_code}" "http://localhost:$port$API_PATH/1")
    http_code="${get_response: -3}"
    if [ "$http_code" = "200" ]; then
        echo "✅ Busca por ID funcionando"
    else
        echo "❌ Busca por ID falhou (HTTP $http_code)"
        return 1
    fi
    
    # Teste 5: Teste de validação (dados inválidos)
    echo "5. Teste de validação:"
    validation_response=$(curl -s -w "%{http_code}" -X POST "http://localhost:$port$API_PATH" \
        -H "Content-Type: application/json" \
        -d '{
            "nomeCliente": "",
            "descricao": "",
            "valor": -10
        }')
    http_code="${validation_response: -3}"
    if [ "$http_code" = "400" ]; then
        echo "✅ Validação funcionando"
    else
        echo "❌ Validação falhou (HTTP $http_code)"
        return 1
    fi
    
    echo "✅ Todos os testes básicos passaram no perfil $profile"
    return 0
}

print_separator
echo "AMBIENTE 1: DESENVOLVIMENTO (H2 Database)"
print_separator

echo "Executando testes unitários e de integração..."
mvn test -Dspring.profiles.active=dev -q

if [ $? -eq 0 ]; then
    echo "✅ Testes automatizados passaram no ambiente de desenvolvimento"
else
    echo "❌ Testes automatizados falharam no ambiente de desenvolvimento"
    exit 1
fi

echo ""
echo "Para testar a aplicação rodando, execute em outro terminal:"
echo "mvn spring-boot:run -Dspring-boot.run.profiles=dev"
echo ""
echo "Deseja testar a aplicação rodando no perfil dev? (y/n)"
read -r test_dev

if [ "$test_dev" = "y" ] || [ "$test_dev" = "Y" ]; then
    echo "Aguardando aplicação no perfil dev..."
    if check_app_running 8080; then
        test_api_basic 8080 "dev"
        if [ $? -eq 0 ]; then
            echo "✅ Testes manuais passaram no ambiente de desenvolvimento"
        else
            echo "❌ Testes manuais falharam no ambiente de desenvolvimento"
        fi
    fi
fi

print_separator
echo "AMBIENTE 2: PRODUÇÃO (PostgreSQL Simulado)"
print_separator

echo "Executando testes com perfil de produção..."
mvn test -Dspring.profiles.active=prod -Dspring.datasource.url=jdbc:h2:mem:prodtest -q

if [ $? -eq 0 ]; then
    echo "✅ Testes automatizados passaram no ambiente de produção simulado"
else
    echo "❌ Testes automatizados falharam no ambiente de produção simulado"
    exit 1
fi

echo ""
echo "Para testar a aplicação rodando em produção, execute em outro terminal:"
echo "export DB_HOST=localhost"
echo "export DB_PORT=5432"
echo "export DB_NAME=pedidos_db"
echo "export DB_USERNAME=postgres"
echo "export DB_PASSWORD=password"
echo "mvn spring-boot:run -Dspring-boot.run.profiles=prod"
echo ""
echo "Deseja testar a aplicação rodando no perfil prod? (y/n)"
read -r test_prod

if [ "$test_prod" = "y" ] || [ "$test_prod" = "Y" ]; then
    echo "Aguardando aplicação no perfil prod..."
    if check_app_running 8080; then
        test_api_basic 8080 "prod"
        if [ $? -eq 0 ]; then
            echo "✅ Testes manuais passaram no ambiente de produção"
        else
            echo "❌ Testes manuais falharam no ambiente de produção"
        fi
    fi
fi

print_separator
echo "AMBIENTE 3: TESTES COM TESTCONTAINERS (PostgreSQL Real)"
print_separator

echo "Executando testes com TestContainers (PostgreSQL real)..."
mvn test -Dtest=PostgreSQLIntegrationTest -q

if [ $? -eq 0 ]; then
    echo "✅ Testes com TestContainers passaram"
else
    echo "❌ Testes com TestContainers falharam (pode ser devido a Docker não disponível)"
fi

print_separator
echo "AMBIENTE 4: TESTES DE COBERTURA"
print_separator

echo "Executando análise de cobertura..."
mvn clean test jacoco:report -q

if [ $? -eq 0 ]; then
    echo "✅ Análise de cobertura concluída"
    echo "📊 Relatório disponível em: target/site/jacoco/index.html"
    
    # Extrair métricas de cobertura
    if [ -f "target/site/jacoco/index.html" ]; then
        coverage=$(grep -o '[0-9]\+%' target/site/jacoco/index.html | head -1)
        echo "📈 Cobertura de instruções: $coverage"
    fi
else
    echo "❌ Análise de cobertura falhou"
fi

print_separator
echo "RESUMO DOS TESTES EM DIFERENTES AMBIENTES"
print_separator

echo "✅ Ambiente de Desenvolvimento (H2): Testado"
echo "✅ Ambiente de Produção Simulado: Testado"
echo "✅ Testes de Cobertura: Executados"
echo "ℹ️  TestContainers: Depende do Docker"

echo ""
echo "ARQUIVOS GERADOS:"
echo "- target/site/jacoco/index.html (Relatório de cobertura)"
echo "- target/surefire-reports/ (Relatórios de teste)"
echo "- logs/ (Logs da aplicação)"

echo ""
echo "PRÓXIMOS PASSOS:"
echo "1. Revisar relatório de cobertura"
echo "2. Executar testes manuais com aplicação rodando"
echo "3. Configurar CI/CD para execução automática"
echo "4. Configurar ambiente de produção real"

print_separator
echo "=== TESTES EM DIFERENTES AMBIENTES CONCLUÍDOS ==="
print_separator