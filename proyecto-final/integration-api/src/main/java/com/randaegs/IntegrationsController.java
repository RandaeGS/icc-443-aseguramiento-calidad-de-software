package com.randaegs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/integrations")
@Produces(MediaType.APPLICATION_JSON)
public class IntegrationsController {

    @GET
    public Response showMessage() {
        return Response.ok("Processes processed").build();
    }
}
