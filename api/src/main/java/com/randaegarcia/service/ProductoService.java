package com.randaegarcia.service;

import com.randaegarcia.domain.model.Producto;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@ApplicationScoped
public class ProductoService {

    public Producto save(@NotNull @Valid Producto producto) {
        producto.persist();
        return producto;
    }

    public Producto findById(Long id) {
        return Producto.findById(id);
    }
}
