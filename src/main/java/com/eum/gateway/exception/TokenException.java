package com.eum.gateway.exception;

public class TokenException extends RuntimeException {
    public TokenException (String message, Throwable cause) {
        super(message);
    }
}
