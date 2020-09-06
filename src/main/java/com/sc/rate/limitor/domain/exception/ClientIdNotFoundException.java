package com.sc.rate.limitor.domain.exception;

public class ClientIdNotFoundException extends RuntimeException {

    public ClientIdNotFoundException(String message, int statusCode) {
        super(String.format("message : %s , statusCode: %d", message, statusCode));
    }
}
