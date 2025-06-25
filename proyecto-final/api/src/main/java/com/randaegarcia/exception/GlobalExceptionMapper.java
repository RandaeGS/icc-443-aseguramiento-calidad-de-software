package com.randaegarcia.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    public Response toResponse(Throwable exception) {
        if (exception instanceof GeneralException) {
            GeneralException businessException = (GeneralException) exception;
            return Response
                    .status(businessException.getStatus())
                    .entity(new ErrorResponse(businessException.getMessage(), businessException.getStatus().getStatusCode()))
                    .build();
        }

        if (exception instanceof NotFoundException){
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Objeto no encontrado")
                    .build();
        }

        // Manejar otras excepciones no controladas
        LOG.error("Error no manejado: ", exception);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error interno del servidor: " + exception.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                .build();
    }
}
