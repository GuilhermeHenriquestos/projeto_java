package com.example.apipedidos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuração de logging e monitoramento
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingConfiguration.class);
    
    /**
     * Filtro para logging de requisições HTTP (apenas em desenvolvimento)
     */
    @Bean
    @Profile("dev")
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        
        log.info("Configurando filtro de logging de requisições para ambiente de desenvolvimento");
        
        return filter;
    }
    

    
    /**
     * Configuração inicial do sistema de logging
     */
    @Bean
    public String loggingSystemInitializer() {
        log.info("=== Sistema de Logging Inicializado ===");
        log.info("Perfil ativo: {}", System.getProperty("spring.profiles.active", "default"));
        log.info("Diretório de logs: {}", System.getProperty("LOG_FILE_PATH", "./logs"));
        log.info("Nível de log da aplicação: {}", System.getProperty("LOG_LEVEL", "INFO"));
        log.info("AOP habilitado para logging de auditoria e performance");
        log.info("========================================");
        
        return "logging-initialized";
    }
}