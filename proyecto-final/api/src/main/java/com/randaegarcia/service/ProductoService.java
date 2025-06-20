package com.randaegarcia.service;

import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.exception.ConflictException;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ProductoService {

    public Response save(@NotNull @Valid Producto producto) {
        if (Producto.findByNombre(producto.getNombre()) != null) {
            throw new ConflictException("Nombre de producto ya existe");
        }
        producto.persist();
        return Response.ok(producto).build();
    }

    public Producto findById(Long id) {
        return Producto.findById(id);
    }
}
