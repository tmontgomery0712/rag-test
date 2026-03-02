package com.example.ragaitest.exception;

import com.example.ragaitest.model.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public final ResponseEntity<ApiError> handleApiException(ApiException ex) {
        String message = "An API error occurred.";
        log.error(message, ex);
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(ex.getStatus(), message, ex.getMessage()));
    }

    @ExceptionHandler
    public final ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials.", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(HttpStatus.FORBIDDEN, "Invalid credentials.", ex.getMessage()));
    }

    @ExceptionHandler
    public final ResponseEntity<ApiError> handleException(Exception ex) {
        String message = "An unexpected error occurred.";
        log.error(message, ex);
        return ResponseEntity.internalServerError().body(
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, ex.getMessage()));
    }

}
