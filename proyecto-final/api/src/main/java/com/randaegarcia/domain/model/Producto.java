package com.randaegarcia.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Audited
public class Producto extends PanacheEntity {

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, unique = true)
    public String name;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false)
    public String description;

    @NotNull
    @Column(nullable = false)
    public String category;

    @PositiveOrZero
    @Column(nullable = false)
    public Double price;

    @PositiveOrZero
    @Column(nullable = false)
    public Double cost;

    @NotNull
    @Column(nullable = false)
    public Double profit;

    @PositiveOrZero
    @Column(nullable = false)
    public Long quantity;

    @NotNull
    @Min(value = 0)
    @PositiveOrZero
    @Column(name = "minimum_stock", nullable = false)
    public Long minimumStock;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive;

    public static List<Producto> findAllPaginated(int page, int size) {
        return find("isActive = true order by id desc")
                .page(page, size)
                .list();
    }

    public static Producto findByNombre(String name){
        return find("name = ?1", name).firstResult();
    }

    public static boolean existsByNombre(String name){
        return count("name = ?1 and isActive = true", name) > 0;
    }

    public static boolean existsById(Long id){
        return findById(id) != null;
    }
}
