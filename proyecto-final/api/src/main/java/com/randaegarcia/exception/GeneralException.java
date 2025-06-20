package com.randaegarcia.exception;

import jakarta.ws.rs.core.Response;

public class GeneralException extends RuntimeException {
    private final Response.Status status;

    public GeneralException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
