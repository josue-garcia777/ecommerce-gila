package com.josue.ecommerce.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.net.URI;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> handleApiException(ApiException exception, HttpServletRequest request) {
        return buildProblemResponse(exception.status(), exception.title(), exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception,
                                                   HttpServletRequest request) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return buildProblemResponse(HttpStatus.BAD_REQUEST, "Request validation failed", detail, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception,
                                                            HttpServletRequest request) {
        String detail = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return buildProblemResponse(HttpStatus.BAD_REQUEST, "Request validation failed", detail, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestPartException.class,
            MultipartException.class})
    ResponseEntity<ProblemDetail> handleMalformedRequest(Exception exception, HttpServletRequest request) {
        return buildProblemResponse(HttpStatus.BAD_REQUEST, "Malformed request", "The request could not be read", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ProblemDetail> handleUploadTooLarge(MaxUploadSizeExceededException exception,
                                                       HttpServletRequest request) {
        return buildProblemResponse(HttpStatus.CONTENT_TOO_LARGE, "File too large",
                "CSV files must not exceed 5 MB", request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> handleOptimisticConflict(ObjectOptimisticLockingFailureException exception,
                                                           HttpServletRequest request) {
        return buildProblemResponse(HttpStatus.CONFLICT, "Concurrent update conflict",
                "The resource changed while the request was being processed", request);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> handleDatabaseConcurrencyConflict(
            PessimisticLockingFailureException exception, HttpServletRequest request) {
        return buildProblemResponse(HttpStatus.CONFLICT, "Concurrent transaction conflict",
                "The transaction could not acquire a database lock; retry with the same idempotency key", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleDataConflict(DataIntegrityViolationException exception,
                                                     HttpServletRequest request) {
        return buildProblemResponse(HttpStatus.CONFLICT, "Data conflict",
                "The request conflicts with existing data", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ProblemDetail> handleMissingResource(NoResourceFoundException exception,
                                                        HttpServletRequest request) {
        return buildProblemResponse(HttpStatus.NOT_FOUND, "Resource not found",
                "No resource exists at the requested path", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected request failure", exception);

        return buildProblemResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error",
                "The request could not be completed", request);
    }

    private ResponseEntity<ProblemDetail> buildProblemResponse(HttpStatus status, String title, String detail,
                                                               HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
