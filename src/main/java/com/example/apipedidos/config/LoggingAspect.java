package com.example.apipedidos.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;

/**
 * Aspecto para logging de auditoria e performance
 */
@Aspect
@Component
public class LoggingAspect {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");
    
    /**
     * Pointcut para todos os métodos dos controllers
     */
    @Pointcut("execution(* com.example.apipedidos.controller.*.*(..))")
    public void controllerMethods() {}
    
    /**
     * Pointcut para todos os métodos dos services
     */
    @Pointcut("execution(* com.example.apipedidos.service.*.*(..))")
    public void serviceMethods() {}
    
    /**
     * Pointcut para métodos de operações CRUD
     */
    @Pointcut("execution(* com.example.apipedidos.service.*.criar*(..)) || " +
              "execution(* com.example.apipedidos.service.*.buscar*(..)) || " +
              "execution(* com.example.apipedidos.service.*.listar*(..)) || " +
              "execution(* com.example.apipedidos.service.*.atualizar*(..)) || " +
              "execution(* com.example.apipedidos.service.*.deletar*(..))")
    public void crudOperations() {}
    
    /**
     * Around advice para logging de performance em controllers
     */
    @Around("controllerMethods()")
    public Object logControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String requestId = generateRequestId();
        
        // Adicionar informações ao MDC
        MDC.put("requestId", requestId);
        MDC.put("method", methodName);
        MDC.put("class", className);
        
        try {
            // Obter informações da requisição HTTP
            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                MDC.put("httpMethod", request.getMethod());
                MDC.put("uri", request.getRequestURI());
                MDC.put("remoteAddr", getClientIpAddress(request));
            }
            
            log.info("Iniciando execução do endpoint: {}.{}", className, methodName);
            
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            performanceLogger.info("ENDPOINT_PERFORMANCE - Class: {}, Method: {}, ExecutionTime: {}ms, RequestId: {}", 
                    className, methodName, executionTime, requestId);
            
            log.info("Endpoint executado com sucesso: {}.{} em {}ms", className, methodName, executionTime);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            performanceLogger.error("ENDPOINT_ERROR - Class: {}, Method: {}, ExecutionTime: {}ms, Error: {}, RequestId: {}", 
                    className, methodName, executionTime, e.getMessage(), requestId);
            
            log.error("Erro na execução do endpoint: {}.{} em {}ms - {}", className, methodName, executionTime, e.getMessage());
            
            throw e;
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Around advice para logging de auditoria em operações CRUD
     */
    @Around("crudOperations()")
    public Object logCrudOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        String requestId = MDC.get("requestId");
        
        // Log de auditoria antes da operação
        auditLogger.info("CRUD_OPERATION_START - Class: {}, Method: {}, Args: {}, RequestId: {}", 
                className, methodName, sanitizeArgs(args), requestId);
        
        try {
            Object result = joinPoint.proceed();
            
            // Log de auditoria após sucesso
            auditLogger.info("CRUD_OPERATION_SUCCESS - Class: {}, Method: {}, RequestId: {}", 
                    className, methodName, requestId);
            
            return result;
            
        } catch (Exception e) {
            // Log de auditoria em caso de erro
            auditLogger.error("CRUD_OPERATION_ERROR - Class: {}, Method: {}, Error: {}, RequestId: {}", 
                    className, methodName, e.getMessage(), requestId);
            
            throw e;
        }
    }
    
    /**
     * After throwing advice para logging detalhado de erros
     */
    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String requestId = MDC.get("requestId");
        
        log.error("Exceção capturada em {}.{} - RequestId: {} - Tipo: {} - Mensagem: {}", 
                className, methodName, requestId, exception.getClass().getSimpleName(), exception.getMessage());
        
        // Log detalhado para debugging (apenas em desenvolvimento)
        if (log.isDebugEnabled()) {
            log.debug("Stack trace completo para {}.{} - RequestId: {}", className, methodName, requestId, exception);
        }
    }
    
    /**
     * Gera um ID único para a requisição
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Obtém a requisição HTTP atual
     */
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }
    
    /**
     * Obtém o endereço IP real do cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Sanitiza argumentos para logging (remove dados sensíveis)
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        return Arrays.toString(args)
                .replaceAll("password=[^,\\]]+", "password=***")
                .replaceAll("senha=[^,\\]]+", "senha=***")
                .replaceAll("token=[^,\\]]+", "token=***");
    }
}