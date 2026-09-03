package com.clinic.doctor.exception;

public class InvalidUserOperationException extends RuntimeException {

    public InvalidUserOperationException(String message) {
        super(message);
    }
}