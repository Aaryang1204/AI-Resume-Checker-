package com.airesume.middleware.exception;

// Thrown by AuthService when a register attempt uses an email that's already in the DB
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}
