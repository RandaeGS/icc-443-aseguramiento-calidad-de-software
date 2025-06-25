package com.randaegarcia.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
public class Producto extends PanacheEntity {

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, unique = true)
    public String nombre;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false)
    public String descripcion;

    @NotNull
    @Column(nullable = false)
    public String categoria;

    @PositiveOrZero
    @Column(nullable = false)
    public Double precio;

    @PositiveOrZero
    @Column(nullable = false)
    public Double costo;

    @NotNull
    @Column(nullable = false)
    public Double beneficio;

    @PositiveOrZero
    @Column(nullable = false)
    public Long cantidadInicial;

    @PositiveOrZero
    @Column
    public Integer impuesto;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "is_borrado")
    public Boolean isBorrado;

    public static List<Producto> findAllPaginated(int page, int size) {
        return find("isBorrado = false order by id desc")
                .page(page, size)
                .list();
    }

    public static Producto findByNombre(String nombre){
        return find("nombre = ?1", nombre).firstResult();
    }

    public static boolean existsByNombre(String nombre){
        return find("nombre = ?1", nombre) != null;
    }

    public static boolean existsById(Long id){
        return findById(id) != null;
    }
}
