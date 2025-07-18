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
        if (producto.quantity < producto.minimumStock){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        producto.isActive = true;
        producto.persist();
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

        // Paso 1: Obtener TODAS las revisiones y procesarlas (para datasets medianos)
        AuditQuery allRevisionsQuery = auditReader.createQuery()
                .forRevisionsOfEntity(Producto.class, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc()); // Orden ascendente para procesamiento

        List<Object[]> allRevisions = allRevisionsQuery.getResultList();

        // Paso 2: Procesar todos los cambios de cantidad
        List<QuantityHistoryDto> allQuantityChanges = processQuantityChanges(allRevisions);

        // Paso 3: Invertir para tener los más recientes primero
        Collections.reverse(allQuantityChanges);

        // Paso 4: Aplicar paginación manual a los resultados procesados
        long totalElements = allQuantityChanges.size();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, allQuantityChanges.size());

        // Validar que el startIndex no esté fuera de rango
        if (startIndex >= totalElements) {
            // Página fuera de rango, devolver página vacía
            PaginatedResponse<QuantityHistoryDto> emptyResponse = PaginatedResponse.of(
                    Collections.emptyList(),
                    page,
                    size,
                    totalElements
            );
            return Response.ok(emptyResponse).build();
        }

        List<QuantityHistoryDto> paginatedContent = allQuantityChanges.subList(startIndex, endIndex);

        // Paso 5: Crear respuesta paginada
        PaginatedResponse<QuantityHistoryDto> response = PaginatedResponse.of(
                paginatedContent,
                page,
                size,
                totalElements
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
