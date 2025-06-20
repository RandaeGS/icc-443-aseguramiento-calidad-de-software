package com.randaegarcia.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    public static Producto findByNombre(String nombre){
        return find("nombre = ?1", nombre).firstResult();
    }
}
