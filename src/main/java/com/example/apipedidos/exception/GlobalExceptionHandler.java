package com.example.apipedidos.exception;

import com.example.apipedidos.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String requestId = MDC.get("requestId");
        String uri = request.getDescription(false).replace("uri=", "");
        
        List<String> details = new ArrayList<>();
        StringBuilder messageBuilder = new StringBuilder();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldError = error.getField() + ": " + error.getDefaultMessage();
            details.add(fieldError);
            if (messageBuilder.length() > 0) {
                messageBuilder.append("; ");
            }
            messageBuilder.append(error.getDefaultMessage());
        });
        
        String message = messageBuilder.length() > 0 ? messageBuilder.toString() : "Dados inválidos fornecidos";
        
        // Log detalhado do erro de validação
        log.warn("Erro de validação - URI: {}, RequestId: {}, Campos inválidos: {}", uri, requestId, details);
        auditLogger.warn("VALIDATION_ERROR - URI: {}, RequestId: {}, Details: {}", uri, requestId, details);
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            uri,
            details
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PedidoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePedidoNotFoundException(
            PedidoNotFoundException ex, WebRequest request) {
        
        String requestId = MDC.get("requestId");
        String uri = request.getDescription(false).replace("uri=", "");
        
        // Log do erro de recurso não encontrado
        log.info("Recurso não encontrado - URI: {}, RequestId: {}, Mensagem: {}", uri, requestId, ex.getMessage());
        auditLogger.info("RESOURCE_NOT_FOUND - URI: {}, RequestId: {}, Message: {}", uri, requestId, ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            uri,
            new ArrayList<>()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErrorResponse> handleDadosInvalidosException(
            DadosInvalidosException ex, WebRequest request) {
        
        String requestId = MDC.get("requestId");
        String uri = request.getDescription(false).replace("uri=", "");
        
        // Log do erro de dados inválidos
        log.warn("Dados inválidos - URI: {}, RequestId: {}, Mensagem: {}", uri, requestId, ex.getMessage());
        auditLogger.warn("INVALID_DATA_ERROR - URI: {}, RequestId: {}, Message: {}", uri, requestId, ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            uri,
            new ArrayList<>()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        String requestId = MDC.get("requestId");
        String uri = request.getDescription(false).replace("uri=", "");
        
        List<String> details = new ArrayList<>();
        StringBuilder messageBuilder = new StringBuilder();
        
        ex.getConstraintViolations().forEach(violation -> {
            String violationError = violation.getPropertyPath() + ": " + violation.getMessage();
            details.add(violationError);
            if (messageBuilder.length() > 0) {
                messageBuilder.append("; ");
            }
            messageBuilder.append(violation.getMessage());
        });
        
        String message = messageBuilder.length() > 0 ? messageBuilder.toString() : "Dados inválidos fornecidos";
        
        // Log detalhado do erro de constraint
        log.warn("Erro de constraint - URI: {}, RequestId: {}, Violações: {}", uri, requestId, details);
        auditLogger.warn("CONSTRAINT_VIOLATION - URI: {}, RequestId: {}, Details: {}", uri, requestId, details);
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            uri,
            details
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        String requestId = MDC.get("requestId");
        String uri = request.getDescription(false).replace("uri=", "");
        String message = "Parâmetro inválido: " + ex.getName() + " deve ser do tipo " + 
                        ex.getRequiredType().getSimpleName();
        
        // Log do erro de tipo de argumento
        log.warn("Erro de tipo de argumento - URI: {}, RequestId: {}, Parâmetro: {}, Tipo esperado: {}, Valor recebido: {}", 
                uri, requestId, ex.getName(), ex.getRequiredType().getSimpleName(), ex.getValue());
        auditLogger.warn("ARGUMENT_TYPE_MISMATCH - URI: {}, RequestId: {}, Parameter: {}, ExpectedType: {}, ReceivedValue: {}", 
                uri, requestId, ex.getName(), ex.getRequiredType().getSimpleName(), ex.getValue());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            uri,
            new ArrayList<>()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        String requestId = MDC.get("requestId");
        String uri = request.getDescription(false).replace("uri=", "");
        
        // Log do erro de JSON malformado
        log.warn("JSON malformado - URI: {}, RequestId: {}, Erro: {}", uri, requestId, ex.getMessage());
        auditLogger.warn("MALFORMED_JSON - URI: {}, RequestId: {}, Error: {}", uri, requestId, ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "JSON malformado ou inválido",
            uri,
            new ArrayList<>()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        String requestId = MDC.get("requestId");
        String uri = request.getDescription(false).replace("uri=", "");
        
        // Log detalhado do erro interno (crítico)
        log.error("Erro interno do servidor - URI: {}, RequestId: {}, Tipo: {}, Mensagem: {}", 
                uri, requestId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        auditLogger.error("INTERNAL_SERVER_ERROR - URI: {}, RequestId: {}, Type: {}, Message: {}", 
                uri, requestId, ex.getClass().getSimpleName(), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Ocorreu um erro interno no servidor",
            uri,
            new ArrayList<>()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}