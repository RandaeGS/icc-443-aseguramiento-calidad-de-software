package com.randaegarcia.controller;

import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.service.ProductoService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GET
    public Response findAll(@QueryParam("page") @DefaultValue("0") int page,  @QueryParam("size") @DefaultValue("10") int size,
                            @QueryParam("name") @DefaultValue("") String name) {
        return productoService.findAll(page, size, name);
    }

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

    @PUT
    @Transactional
    public Response update(@NotNull @Valid Producto producto) {
        return productoService.update(producto);
    }

    @Path("{id}")
    @DELETE
    @Transactional
    public Response deleteProducto(@PathParam("id") Long id) {
        return productoService.deleteProducto(id);
    }
}
