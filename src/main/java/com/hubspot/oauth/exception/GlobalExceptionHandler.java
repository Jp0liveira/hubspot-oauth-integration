package com.hubspot.oauth.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            TokenExchangeException.class,
            RateLimitExceededException.class,
            InvalidTokenException.class,
            NoTokenFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusinessExceptions(RuntimeException ex) {
        logger.error("Erro de negócio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "BUSINESS_ERROR",
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(InvalidSignatureException.class)
    public ResponseEntity<ApiErrorResponse> handleSecurityExceptions(InvalidSignatureException ex) {
        logger.warn("Erro de segurança: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "SECURITY_ERROR",
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler({
            Exception.class,
            NoSuchAlgorithmException.class,
            JsonProcessingException.class
    })
    public ResponseEntity<ApiErrorResponse> handleGenericExceptions(Exception ex) {
        logger.error("Erro interno: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_ERROR",
                        "Ocorreu um erro interno",
                        ex.getLocalizedMessage()
                ));
    }

}
