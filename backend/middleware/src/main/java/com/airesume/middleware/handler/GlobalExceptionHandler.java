package com.airesume.middleware.handler;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.airesume.middleware.dto.response.BaseResponse;
import com.airesume.middleware.exception.EmailAlreadyExistsException;
import com.airesume.middleware.exception.InvalidCredentialsException;

// Intercepts exceptions thrown anywhere in @RestController classes and converts
// them into a consistent BaseResponse JSON body — controllers stay clean.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Fires when @Valid fails on a @RequestBody — collects all field error messages
    // and joins them so the frontend knows exactly which fields are wrong
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                // getDefaultMessage() is the message we wrote in @NotBlank, @Email, etc.
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .badRequest()
                .body(BaseResponse.badRequest(message));
    }

    // Duplicate email on register → 409 Conflict
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<BaseResponse<Void>> handleEmailConflict(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(BaseResponse.conflict(ex.getMessage()));
    }

    // Wrong password or email on login → 401 Unauthorized
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.unauthorized(ex.getMessage()));
    }

    // Catch-all so unhandled exceptions still return BaseResponse instead of Spring's default error page
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(500, "Something went wrong: " + ex.getMessage()));
    }
}
