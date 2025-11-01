package com.example.apipedidos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Indicador de saúde do sistema de logging
 */
@Component
public class LoggingHealthIndicator implements HealthIndicator {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingHealthIndicator.class);
    
    @Override
    public Health health() {
        try {
            Health.Builder healthBuilder = Health.up();
            
            // Verificar se o diretório de logs existe e é acessível
            String logPath = System.getProperty("LOG_FILE_PATH", "./logs");
            Path logsDir = Paths.get(logPath).getParent();
            
            if (logsDir != null) {
                if (!Files.exists(logsDir)) {
                    Files.createDirectories(logsDir);
                    log.info("Diretório de logs criado: {}", logsDir.toAbsolutePath());
                }
                
                if (Files.isWritable(logsDir)) {
                    healthBuilder.withDetail("logsDirectory", logsDir.toAbsolutePath().toString())
                              .withDetail("logsDirectoryWritable", true);
                } else {
                    healthBuilder.down()
                              .withDetail("logsDirectory", logsDir.toAbsolutePath().toString())
                              .withDetail("logsDirectoryWritable", false)
                              .withDetail("error", "Diretório de logs não é gravável");
                }
            }
            
            // Verificar arquivos de log existentes
            File logFile = new File(logPath + ".log");
            File auditLogFile = new File(logPath + "-audit.log");
            File performanceLogFile = new File(logPath + "-performance.log");
            
            healthBuilder.withDetail("mainLogFile", logFile.exists() ? logFile.getAbsolutePath() : "Não criado ainda")
                        .withDetail("auditLogFile", auditLogFile.exists() ? auditLogFile.getAbsolutePath() : "Não criado ainda")
                        .withDetail("performanceLogFile", performanceLogFile.exists() ? performanceLogFile.getAbsolutePath() : "Não criado ainda");
            
            // Verificar configuração de logging
            healthBuilder.withDetail("activeProfile", System.getProperty("spring.profiles.active", "default"))
                        .withDetail("logLevel", System.getProperty("LOG_LEVEL", "INFO"))
                        .withDetail("aspectsEnabled", true);
            
            return healthBuilder.build();
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do sistema de logging", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}