package com.randaegarcia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randaegarcia.domain.dto.PaginatedResponse;
import com.randaegarcia.domain.dto.ProductoListRequestDto;
import com.randaegarcia.domain.dto.QuantityHistoryDto;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.domain.model.StockMovement;
import com.randaegarcia.domain.model.StockMovement$;
import com.randaegarcia.exception.ConflictException;
import com.randaegarcia.exception.StockExceededException;
import com.randaegarcia.security.CustomRevisionEntity;
import com.speedment.jpastreamer.application.JPAStreamer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ProductoService {
    private final JPAStreamer jpaStreamer;
    private final EntityManager em;
    private final JsonWebToken jwt;

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
        if (producto.quantity < producto.minimumStock){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        producto.isActive = true;
        producto.persist();

        var stockMovement = new StockMovement();
        stockMovement.producto = producto;
        stockMovement.date = LocalDateTime.now();
        stockMovement.quantityChange = producto.quantity;
        stockMovement.actualQuantity = producto.quantity;
        stockMovement.username = jwt.getClaim("name");
        stockMovement.persist();
        return Response.ok(producto).build();
    }

    public Producto findById(Long id) {
        return Producto.find("id = ?1 and isActive = true", id).firstResult();
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
        if (oldProducto.quantity != producto.quantity) {
            var stockMovement = new StockMovement();
            stockMovement.producto = producto;
            stockMovement.date = LocalDateTime.now();
            stockMovement.quantityChange = Math.max(oldProducto.quantity, producto.quantity) - Math.min(oldProducto.quantity, producto.quantity);
            stockMovement.actualQuantity = producto.quantity;
            stockMovement.username = jwt.getClaim("name");
            stockMovement.persist();
        }
        oldProducto.quantity = producto.quantity;

        return Response.ok(oldProducto).build();
    }

    public Response deleteProducto(Long id) {
        Producto producto = Producto.findById(id);
        if (producto == null || !producto.isActive) {
            throw new NotFoundException("Product not found");
        }
        producto.isActive = false;
        producto.persist();
        return Response.ok(producto).build();
    }

    public Response getQuantityHistory(Long id, int page, int size) {
        Producto producto = Producto.find("id = ?1 and isActive = true", id).firstResult();
        if (producto == null || !producto.isActive) {
            throw new NotFoundException("Product not found");
        }

        List<StockMovement> stockMovements = jpaStreamer.stream(StockMovement.class)
                .filter(stockMovement -> stockMovement.producto.id.equals(id))
                .sorted(StockMovement$.date.reversed())
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());

        PaginatedResponse<StockMovement> response = PaginatedResponse.of(
                stockMovements,
                page,
                size,
                StockMovement.count("producto = ?1", producto)
        );
        return Response.ok(response).build();
    }

    public Response updateQuantity(@NotNull Long idProducto, @NotNull Long quantity) {
        Producto producto = Producto.findById(idProducto);
        if (producto == null || !producto.isActive) {
            throw new NotFoundException("Product not found");
        }

        if (producto.quantity + quantity < producto.minimumStock) {
            throw new StockExceededException("Minimum stock exceeded");
        }

        var stockMovement = new StockMovement();
        stockMovement.producto = producto;
        stockMovement.actualQuantity = producto.quantity;
        stockMovement.username = jwt.getClaim("name");
        stockMovement.date = LocalDateTime.now();
        stockMovement.quantityChange = quantity;
        producto.quantity += quantity;
        stockMovement.actualQuantity = producto.quantity;
        stockMovement.persist();
        return Response.ok(producto).build();
    }
}
