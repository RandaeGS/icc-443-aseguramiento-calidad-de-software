package com.randaegarcia.exception;

import jakarta.ws.rs.core.Response;

public class ConflictException extends GeneralException {
    public ConflictException(String message) {
        super(Response.Status.CONFLICT, message);
    }
}
