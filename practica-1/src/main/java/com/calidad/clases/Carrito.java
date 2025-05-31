package com.calidad.clases;

import lombok.Data;

import java.util.List;

@Data
public class Carrito {
    private List<ItemCarrito> productos;
    private Double total;
}
