package com.randaegarcia.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
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

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "is_active")
    public Boolean isActive;

    public static List<Producto> findAllPaginated(int page, int size) {
        return find("isActive = false order by id desc")
                .page(page, size)
                .list();
    }

    public static Producto findByNombre(String name){
        return find("name = ?1", name).firstResult();
    }

    public static boolean existsByNombre(String name){
        return count("name = ?1 and isActive = false", name) > 0;
    }

    public static boolean existsById(Long id){
        return findById(id) != null;
    }
}
