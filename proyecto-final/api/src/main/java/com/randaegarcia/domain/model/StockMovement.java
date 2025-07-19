package com.randaegarcia.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "stock_movements")
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement extends PanacheEntity {
    @ManyToOne
    public Producto producto;
    public String username;
    public LocalDateTime date;
    public Long actualQuantity;
    public Long quantityChange;
}
