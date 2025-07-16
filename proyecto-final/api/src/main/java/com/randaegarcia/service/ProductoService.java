package com.randaegarcia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randaegarcia.domain.dto.PaginatedResponse;
import com.randaegarcia.domain.dto.ProductoListRequestDto;
import com.randaegarcia.domain.dto.QuantityHistoryDto;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.exception.ConflictException;
import com.randaegarcia.security.CustomRevisionEntity;
import com.speedment.jpastreamer.application.JPAStreamer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ProductoService {
    private final JPAStreamer jpaStreamer;
    private final EntityManager em;

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

    public Response getQuantityHistory(Long id, int page, int size) {
        if (!Producto.existsById(id)) {
            throw new NotFoundException("Producto no encontrado");
        }

        // Validar parámetros de paginación
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }

        AuditReader auditReader = AuditReaderFactory.get(em);

        // Paso 1: Obtener conteo total de revisiones
        AuditQuery countQuery = auditReader.createQuery()
                .forRevisionsOfEntity(Producto.class, false, false)
                .add(AuditEntity.id().eq(id));

        long totalRevisions = countQuery.getResultList().size();

        // Paso 2: Calcular paginación inversa (más recientes primero)
        // Como queremos los más recientes primero, calculamos desde el final
        int totalPages = (int) Math.ceil((double) totalRevisions / size);
        int startFromEnd = page * size;
        int endFromEnd = startFromEnd + size;

        // Convertir a índices desde el inicio
        int startIndex = Math.max(0, (int) totalRevisions - endFromEnd);
        int querySize = Math.min(size + 1, (int) totalRevisions - startIndex); // +1 para calcular cambios

        // Paso 3: Obtener revisiones paginadas
        AuditQuery paginatedQuery = auditReader.createQuery()
                .forRevisionsOfEntity(Producto.class, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc())
                .setFirstResult(startIndex)
                .setMaxResults(querySize);

        List<Object[]> revisions = paginatedQuery.getResultList();

        // Paso 4: Procesar solo cambios de cantidad
        List<QuantityHistoryDto> quantityChanges = processQuantityChanges(revisions);

        // Paso 5: Revertir para mostrar más reciente primero y aplicar límite
        Collections.reverse(quantityChanges);
        int actualSize = Math.min(size, quantityChanges.size());
        List<QuantityHistoryDto> finalContent = quantityChanges.subList(0, actualSize);

        // Paso 6: Crear respuesta paginada
        PaginatedResponse<QuantityHistoryDto> response = PaginatedResponse.of(
                finalContent,
                page,
                size,
                totalRevisions
        );

        return Response.ok(response).build();
    }

    // Metodo auxiliar para procesar cambios de cantidad
    private List<QuantityHistoryDto> processQuantityChanges(List<Object[]> revisions) {
        List<QuantityHistoryDto> quantityChanges = new ArrayList<>();
        Long previousQuantity = null;

        for (Object[] revision : revisions) {
            Producto producto = (Producto) revision[0];
            CustomRevisionEntity revisionEntity = (CustomRevisionEntity) revision[1];

            // Extraer información de tu CustomRevisionEntity
            String username = revisionEntity.getUsername();
            long timestamp = revisionEntity.getRevtstmp();

            // Solo agregar si hay cambio en cantidad
            if (previousQuantity == null || !previousQuantity.equals(producto.quantity)) {

                Long quantityChange = previousQuantity != null ?
                        producto.quantity - previousQuantity :
                        null; // Primera revisión

                LocalDateTime revisionDate = Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                quantityChanges.add(new QuantityHistoryDto(
                        username,
                        revisionDate,
                        producto.quantity,
                        previousQuantity,
                        quantityChange
                ));
            }

            previousQuantity = producto.quantity;
        }

        return quantityChanges;
    }

    public Response updateQuantity(@NotNull Long idProducto, @NotNull Long quantity) {
        Producto producto = Producto.findById(idProducto);
        if (producto == null || !producto.isActive) {
            throw new NotFoundException("Producto no encontrado");
        }
        producto.quantity += quantity;
        return Response.ok(producto).build();
    }
}
