package com.randaegarcia.controller;

import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.service.ProductoService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @Path("{id}")
    @GET
    public Producto findProductoById(@PathParam("id") Long id) {
        return productoService.findById(id);
    }

    @POST
    @Transactional
    public Producto createProducto(@NotNull @Valid Producto producto) {
        return productoService.save(producto);
    }
}
