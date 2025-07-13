package com.randaegarcia.service;

import com.randaegarcia.domain.dto.PaginatedResponse;
import com.randaegarcia.domain.dto.ProductoListRequestDto;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.exception.ConflictException;
import com.speedment.jpastreamer.application.JPAStreamer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ProductoService {
    private final JPAStreamer jpaStreamer;

    public Response findAll(ProductoListRequestDto requestDto) {
        Supplier<Stream<Producto>> filteredStreamSupplier = () ->
                jpaStreamer.stream(Producto.class)
                        .filter(Producto::getIsActive)
                        .filter(producto -> producto.getName().toLowerCase().startsWith(requestDto.name().toLowerCase()))
                        .filter(producto -> producto.getCategory().equals(requestDto.category()) || requestDto.category().isEmpty())
                        .filter(producto -> producto.getPrice() >= requestDto.minPrice() && (producto.getPrice() <= requestDto.maxPrice() || requestDto.maxPrice() == -1));

        // Conteo total con filtros aplicados
        long total = filteredStreamSupplier.get().count();

        List<Producto> productoList = filteredStreamSupplier.get()
                .sorted((o1, o2) -> o2.id.compareTo(o1.id))
                .skip((long) requestDto.page() * requestDto.size())
                .limit(requestDto.size())
                .toList();

        PaginatedResponse<Producto> response = PaginatedResponse.of(
                productoList,
                requestDto.page(),
                requestDto.size(),
                total
        );

        jpaStreamer.close();
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
