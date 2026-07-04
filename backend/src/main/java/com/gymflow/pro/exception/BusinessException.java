package com.gymflow.pro.exception;

/**
 * Thrown when a request violates a domain/business rule
 * (e.g. duplicate enrollment, insufficient stock, invalid state transition).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
