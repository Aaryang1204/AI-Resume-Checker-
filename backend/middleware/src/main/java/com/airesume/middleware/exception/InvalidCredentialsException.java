package com.airesume.middleware.exception;

// Thrown by AuthService when the password doesn't match the stored BCrypt hash
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
