# Environment Variables Configuration

This document describes the environment variables that can be used to configure the API de Pedidos application.

## Database Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_HOST` | Database host | localhost | No |
| `DB_PORT` | Database port | 5432 | No |
| `DB_NAME` | Database name | pedidos_db | No |
| `DB_USERNAME` | Database username | pedidos_user | No |
| `DB_PASSWORD` | Database password | - | Yes (prod) |
| `DB_POOL_SIZE` | Maximum connection pool size | 20 | No |
| `DB_POOL_MIN_IDLE` | Minimum idle connections | 5 | No |

## Server Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SERVER_PORT` | Server port | 8080 | No |
| `SERVER_MAX_THREADS` | Maximum server threads | 200 | No |
| `SERVER_MIN_THREADS` | Minimum server threads | 10 | No |

## Application Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | dev | No |
| `LOG_LEVEL` | Application log level | INFO | No |
| `LOG_FILE_PATH` | Log file path | ./logs/api-pedidos.log | No |
| `METRICS_ENABLED` | Enable Prometheus metrics | false | No |

## Example Production Configuration

```bash
# Database
export DB_HOST=prod-db-server.example.com
export DB_PORT=5432
export DB_NAME=pedidos_production
export DB_USERNAME=pedidos_app
export DB_PASSWORD=secure_password_here

# Server
export SERVER_PORT=8080
export SERVER_MAX_THREADS=300
export SERVER_MIN_THREADS=20

# Application
export SPRING_PROFILES_ACTIVE=prod
export LOG_LEVEL=INFO
export LOG_FILE_PATH=/var/log/api-pedidos/application.log
export METRICS_ENABLED=true
```

## Profile-Specific Behavior

### Development Profile (`dev`)
- Uses H2 in-memory database
- Enables H2 console at `/h2-console`
- Detailed logging with SQL statements
- All management endpoints exposed

### Production Profile (`prod`)
- Uses PostgreSQL database
- File-based logging with rotation
- Optimized connection pooling
- Limited management endpoints
- Requires environment variables for database connection