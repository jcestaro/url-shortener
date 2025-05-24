package com.github.jcestaro.url_shortener.infra.advice;

import com.github.jcestaro.url_shortener.infra.exception.UrlNotFoundException;
import org.apache.kafka.common.KafkaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.requestreply.KafkaReplyTimeoutException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Object> handleUrlNotFoundException(UrlNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ListenerExecutionFailedException.class, ExecutionException.class})
    public ResponseEntity<Object> handleListenerExecutionFailedException(Exception ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof UrlNotFoundException) {
            return handleUrlNotFoundException((UrlNotFoundException) cause);
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Kafka listener failed: " + ex.getMessage());
    }

    @ExceptionHandler({KafkaException.class, KafkaReplyTimeoutException.class})
    public ResponseEntity<Object> handleKafkaExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error communicating with Kafka: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error: " + ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}
