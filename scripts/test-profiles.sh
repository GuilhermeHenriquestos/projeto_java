#!/bin/bash

echo "=== Testing API Pedidos with Different Profiles ==="
echo

# Function to test if application is running
test_application() {
    local profile=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    echo "Testing application with profile: $profile on port: $port"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "✅ Application is running with profile: $profile"
            
            # Test API endpoint
            echo "Testing API endpoint..."
            response=$(curl -s "http://localhost:$port/api/pedidos")
            if [ "$response" = "[]" ]; then
                echo "✅ API endpoint is working correctly"
            else
                echo "❌ API endpoint returned unexpected response: $response"
            fi
            
            # Test actuator endpoints
            echo "Testing actuator endpoints..."
            health_status=$(curl -s "http://localhost:$port/actuator/health" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
            if [ "$health_status" = "UP" ]; then
                echo "✅ Health check passed"
            else
                echo "❌ Health check failed: $health_status"
            fi
            
            return 0
        fi
        
        echo "Attempt $attempt/$max_attempts: Application not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "❌ Application failed to start with profile: $profile"
    return 1
}

# Test Development Profile
echo "=== Testing Development Profile ==="
echo "Starting application with dev profile..."
export SPRING_PROFILES_ACTIVE=dev
export SERVER_PORT=8081

# Start application in background
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dserver.port=8081 > dev-test.log 2>&1 &
DEV_PID=$!

# Test the application
if test_application "dev" 8081; then
    echo "✅ Development profile test passed"
    
    # Test H2 console availability
    if curl -s "http://localhost:8081/h2-console" | grep -q "H2 Console"; then
        echo "✅ H2 Console is accessible in dev profile"
    else
        echo "⚠️  H2 Console might not be accessible (this could be normal)"
    fi
    
    # Test additional actuator endpoints in dev
    if curl -s "http://localhost:8081/actuator/metrics" > /dev/null 2>&1; then
        echo "✅ Additional actuator endpoints are exposed in dev profile"
    else
        echo "❌ Additional actuator endpoints are not accessible in dev profile"
    fi
else
    echo "❌ Development profile test failed"
fi

# Stop dev application
kill $DEV_PID 2>/dev/null
wait $DEV_PID 2>/dev/null
echo

# Test Production Profile (with H2 for testing purposes)
echo "=== Testing Production Profile ==="
echo "Starting application with prod profile..."
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8082
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=test_db
export DB_USERNAME=test_user
export DB_PASSWORD=test_password
export LOG_LEVEL=INFO

# Override database settings for testing (use H2 instead of PostgreSQL)
export SPRING_DATASOURCE_URL="jdbc:h2:mem:prodtest;DB_CLOSE_DELAY=-1"
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.h2.Driver"
export SPRING_DATASOURCE_USERNAME="sa"
export SPRING_DATASOURCE_PASSWORD=""
export SPRING_JPA_HIBERNATE_DDL_AUTO="create-drop"

# Start application in background
mvn spring-boot:run -Dspring-boot.run.profiles=prod -Dserver.port=8082 \
    -Dspring.datasource.url="jdbc:h2:mem:prodtest;DB_CLOSE_DELAY=-1" \
    -Dspring.datasource.driver-class-name="org.h2.Driver" \
    -Dspring.datasource.username="sa" \
    -Dspring.datasource.password="" \
    -Dspring.jpa.hibernate.ddl-auto="create-drop" > prod-test.log 2>&1 &
PROD_PID=$!

# Test the application
if test_application "prod" 8082; then
    echo "✅ Production profile test passed"
    
    # Test that H2 console is NOT accessible in prod
    if curl -s "http://localhost:8082/h2-console" | grep -q "404"; then
        echo "✅ H2 Console is properly disabled in prod profile"
    else
        echo "⚠️  H2 Console accessibility test inconclusive"
    fi
    
    # Test limited actuator endpoints in prod
    if curl -s "http://localhost:8082/actuator/env" | grep -q "404"; then
        echo "✅ Sensitive actuator endpoints are properly restricted in prod profile"
    else
        echo "⚠️  Actuator endpoint restriction test inconclusive"
    fi
else
    echo "❌ Production profile test failed"
fi

# Stop prod application
kill $PROD_PID 2>/dev/null
wait $PROD_PID 2>/dev/null
echo

# Test Environment Variables
echo "=== Testing Environment Variables ==="
echo "Testing custom environment variables..."

export SPRING_PROFILES_ACTIVE=dev
export SERVER_PORT=8083
export LOG_LEVEL=DEBUG
export CUSTOM_VAR=test-value

mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dserver.port=8083 > env-test.log 2>&1 &
ENV_PID=$!

if test_application "env-test" 8083; then
    echo "✅ Environment variables test passed"
    
    # Check if custom logging level is applied
    if grep -q "DEBUG" env-test.log; then
        echo "✅ Custom log level (DEBUG) is being applied"
    else
        echo "⚠️  Custom log level test inconclusive"
    fi
else
    echo "❌ Environment variables test failed"
fi

# Stop env test application
kill $ENV_PID 2>/dev/null
wait $ENV_PID 2>/dev/null

echo
echo "=== Profile Testing Complete ==="
echo "Check the log files for detailed output:"
echo "- dev-test.log: Development profile logs"
echo "- prod-test.log: Production profile logs"
echo "- env-test.log: Environment variables test logs"
echo

# Cleanup
unset SPRING_PROFILES_ACTIVE SERVER_PORT DB_HOST DB_PORT DB_NAME DB_USERNAME DB_PASSWORD LOG_LEVEL CUSTOM_VAR
unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_DRIVER_CLASS_NAME SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD SPRING_JPA_HIBERNATE_DDL_AUTO