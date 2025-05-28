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
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());
    private static final String UNEXPECTED_SERVER_ERROR = "Unexpected server error";

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Object> handleUrlNotFoundException(UrlNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ListenerExecutionFailedException.class, ExecutionException.class})
    public ResponseEntity<Object> handleListenerExecutionFailedException(Exception ex) {
        LOGGER.warning("Kafka listener failed: " + ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_SERVER_ERROR);
    }

    @ExceptionHandler({KafkaException.class, KafkaReplyTimeoutException.class})
    public ResponseEntity<Object> handleKafkaExceptions(Exception ex) {
        LOGGER.warning("Error communicating with Kafka: " + ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        LOGGER.warning(UNEXPECTED_SERVER_ERROR + ": " + ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_SERVER_ERROR);
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
