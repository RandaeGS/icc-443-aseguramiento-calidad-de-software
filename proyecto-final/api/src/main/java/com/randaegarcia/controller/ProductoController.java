package com.randaegarcia.controller;

import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.service.ProductoService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GET
    @Path("{id}")
    public Producto findProductoById(@PathParam("id") Long id) {
        return productoService.findById(id);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProducto(@NotNull @Valid Producto producto) {
        return productoService.save(producto);
    }
}
