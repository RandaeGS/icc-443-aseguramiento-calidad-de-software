package com.calidad.services;

import com.calidad.clases.Carrito;
import com.calidad.clases.ItemCarrito;
import com.calidad.clases.Producto;

public class CarritoService {

    public Carrito addItemToCarrito(Producto producto, Carrito carrito, int cantidad) {
        if (producto == null || carrito == null || cantidad <= 0) {
            throw new IllegalArgumentException("El producto, carrito o cantidad no puede ser nulo");
        }

        boolean encontrado = false;
        for (ItemCarrito itemCarrito : carrito.getProductos()){
            if (itemCarrito.getProducto().equals(producto)) {
                itemCarrito.setCantidad(itemCarrito.getCantidad() + cantidad);
                encontrado = true;
            }
        }

        if (!encontrado) {
            ItemCarrito itemCarrito = new ItemCarrito(producto, cantidad);
            carrito.getProductos().add(itemCarrito);
        }

        return carrito;
    }

    public Carrito removeProductoFromCarrito(Producto producto, Carrito carrito) {
        if (producto == null || carrito == null) {
            throw new IllegalArgumentException("El producto o carrito no puede ser nulo");
        }

        ItemCarrito itemCarrito = null;
        for (ItemCarrito items : carrito.getProductos()){
            if (items.getProducto().equals(producto)) {
                itemCarrito = items;
            }
        }
        carrito.getProductos().remove(itemCarrito);
        return carrito;
    }

    public Carrito modifyCarritoItem(Producto producto, Carrito carrito, int cantidad) {
        if (producto == null || carrito == null || cantidad <= 0) {
            throw new IllegalArgumentException("El producto, carrito o cantidad no puede ser nulo");
        }

        for (ItemCarrito items : carrito.getProductos()){
            if (items.getProducto().equals(producto)) {
                items.setCantidad(cantidad);
            }
        }

        return carrito;
    }

    public Double calculateTotalPrice(Carrito carrito) {
        if (carrito == null) {
            throw new IllegalArgumentException("El carrito carrito no puede ser nulo");
        }

        double total = 0.0;

        for (ItemCarrito items : carrito.getProductos()){
            total += items.getProducto().getPrecio() * items.getCantidad();
        }

        return total;
    }
}
