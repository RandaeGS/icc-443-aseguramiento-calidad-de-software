package com.randaegarcia.service;

import com.randaegarcia.domain.dto.PaginatedResponse;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.exception.ConflictException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
public class ProductoService {

    public Response findAll(int page, int size) {
        List<Producto> productoList = Producto.findAllPaginated(page, size);
        long total = Producto.find("isActive", true).count();

        PaginatedResponse<Producto> response = PaginatedResponse.of(productoList, page, size, total);
        return Response.ok(response).build();
    }

    public Response save(@NotNull @Valid Producto producto) {
        if (Producto.existsByNombre(producto.name)) {
            throw new ConflictException("Nombre de producto ya existe");
        }
        producto.isActive = true;
        producto.persist();
        return Response.ok(producto).build();
    }

    public Producto findById(Long id) {
        return Producto.findById(id);
    }

    public Response update(@NotNull @Valid Producto producto) {
        Producto oldProducto = Producto.findById(producto.id);
        if (oldProducto == null) {
            throw new NotFoundException("Producto no encontrado");
        }
        oldProducto.name = producto.name;
        oldProducto.description = producto.description;
        oldProducto.category = producto.category;
        oldProducto.price = producto.price;
        oldProducto.cost = producto.cost;
        oldProducto.profit = producto.profit;
        oldProducto.quantity = producto.quantity;

        return Response.ok(oldProducto).build();
    }

    public Response deleteProducto(Long id) {
        Producto producto = Producto.findById(id);
        if (producto == null || !producto.isActive) {
            throw new NotFoundException("Producto no encontrado");
        }
        producto.isActive = false;
        producto.persist();
        return Response.ok(producto).build();
    }
}
