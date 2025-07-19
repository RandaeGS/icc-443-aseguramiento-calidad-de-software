package com.randaegarcia.exception;

import jakarta.ws.rs.core.Response;

public class StockExceededException extends GeneralException {
    public StockExceededException(String message) {
        super(Response.Status.BAD_REQUEST, message);
    }
}
