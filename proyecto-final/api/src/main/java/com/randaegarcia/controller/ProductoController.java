package com.randaegarcia.controller;

import com.randaegarcia.domain.dto.ProductoListRequestDto;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.service.ProductoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GET
    public Response findAll(@QueryParam("page") @DefaultValue("0") int page,  @QueryParam("size") @DefaultValue("10") int size,
                            @QueryParam("name") @DefaultValue("") String name, @QueryParam("category") @DefaultValue("") String category,
                            @QueryParam("minPrice") @DefaultValue("0")  double minPrice, @QueryParam("maxPrice") @DefaultValue("-1") double maxPrice) {

        ProductoListRequestDto requestDto = new ProductoListRequestDto(page, size, name, category, minPrice, maxPrice);
        return productoService.findAll(requestDto);
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

    @Path("{id}/history")
    @GET
    public Response findProductoHistory(@NotNull @PathParam("id") Long id,
                                        @QueryParam("page") @DefaultValue("0") int page,
                                        @QueryParam("size") @DefaultValue("10") int size) {
        return productoService.getQuantityHistory(id, page, size);
    }

    @Path("{id}/update-quantity")
    @PUT
    @Transactional
    public Response updateProductoQuantity(@NotNull @PathParam("id") Long idProducto, @NotNull @QueryParam("quantity") Long quantity) {
        return productoService.updateQuantity(idProducto, quantity);
    }
}
