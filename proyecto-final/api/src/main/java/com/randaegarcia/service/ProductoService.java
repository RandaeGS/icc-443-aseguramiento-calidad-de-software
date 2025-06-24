package com.randaegarcia.service;

import com.randaegarcia.domain.dto.PaginatedResponse;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.exception.ConflictException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
public class ProductoService {

    public Response findAll(int page, int size) {
        List<Producto> productoList = Producto.findAllPaginated(page, size);
        PaginatedResponse<Producto> response = PaginatedResponse.of(productoList, page, size, Producto.count());
        return Response.ok(response).build();
    }

    public Response save(@NotNull @Valid Producto producto) {
        if (Producto.findByNombre(producto.getNombre()) != null) {
            throw new ConflictException("Nombre de producto ya existe");
        }
        producto.isBorrado = false;
        producto.persist();
        return Response.ok(producto).build();
    }

    public Producto findById(Long id) {
        return Producto.findById(id);
    }
}
