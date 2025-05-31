import com.calidad.clases.Carrito;
import com.calidad.clases.ItemCarrito;
import com.calidad.clases.Producto;
import com.calidad.services.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CarritoServiceTest {

    CarritoService carritoService = new CarritoService();
    Carrito carrito = new Carrito();
    Producto producto = new Producto();
    ItemCarrito itemCarrito = new ItemCarrito();

    @BeforeEach
    void setUp() {
        producto.setNombre("Pendrive");
        producto.setPrecio(400.0);

        itemCarrito.setProducto(producto);
        itemCarrito.setCantidad(0);

        carrito.setProductos(new ArrayList<>());
        carrito.getProductos().add(itemCarrito);
    }

    /**
     * Validar que el servicio de carrito no acepte valores nulos o cantidades negativas y anada un producto
     * correctamente a un carrito con una cantidad dada
     * La cantidad resultante debe de ser a la cantidad pasada como parametro ya que producto.cantidad = 0
     */
    @Test
    public void validarAddToCarrito(){
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.addItemToCarrito(null, null, 5);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.addItemToCarrito(producto, null, 8);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.addItemToCarrito(producto, carrito, 0);
        });

        Producto alfombra = new Producto("alfombra", 550.0);
        ItemCarrito itemAlfombra = new ItemCarrito(alfombra, 5);

        Carrito resultado = carritoService.addItemToCarrito(alfombra, carrito, 5);
        assertEquals(5, resultado.getProductos().getLast().getCantidad());

        resultado = carritoService.addItemToCarrito(producto, carrito, 3);
        assertEquals(3, resultado.getProductos().getFirst().getCantidad());
    }

    /**
     * Validar que la funcion modifyCarrito funciona correctamente
     * No acepta valores nulos o iguales a cero en sus argumentos
     * Debe de asignar la cantidad pasada como argumento al Itemproducto del carrito
     * No puede ser mayor o igual a 0
     */
    @Test
    public void validarModifyCarrito(){
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.modifyCarritoItem(null, null, 5);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.addItemToCarrito(producto, null, 8);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.addItemToCarrito(producto, carrito, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.modifyCarritoItem(producto, carrito, -10);
        });

        Carrito resultado = carritoService.modifyCarritoItem(producto, carrito, 5);
        assertEquals(5, resultado.getProductos().getFirst().getCantidad());

        resultado = carritoService.modifyCarritoItem(producto, carrito, 2);
        assertEquals(2, resultado.getProductos().getFirst().getCantidad());
    }

    /**
     * Valida que el servicio RemoveProductoFromCarrito funcione correctamente
     * No acepta valores nulos en sus parametros
     * El funcionamiento se valida comparando el size de la lista de ItemProductos en el carrito
     */
    @Test
    public void validarRemoveProductoFromCarrito(){
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.removeProductoFromCarrito(null, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.removeProductoFromCarrito(producto, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            carritoService.removeProductoFromCarrito(null, carrito);
        });

        Producto alfombra = new Producto("alfombra", 550.0);
        carritoService.addItemToCarrito(alfombra, carrito, 5);

        Carrito resultado = carritoService.removeProductoFromCarrito(alfombra, carrito);
        assertEquals(1, resultado.getProductos().size());

        resultado = carritoService.removeProductoFromCarrito(producto, carrito);
        assertEquals(0, resultado.getProductos().size());
    }


    /**
     * Valida que el servicio calculateTotalPrice devuelva los valores correctos
     * El valor devuelto debe ser la cantidad de cada producto de la lista por su precio
     */
    @Test
    public void validateTotalCarrito(){
        assertEquals(0, carritoService.calculateTotalPrice(carrito));

        itemCarrito.setCantidad(1);
        assertEquals(400, carritoService.calculateTotalPrice(carrito));

        itemCarrito.setCantidad(10);
        assertEquals(4000, carritoService.calculateTotalPrice(carrito));

        Producto alfombra = new Producto("alfombra", 550.0);
        ItemCarrito itemAlfombra = new ItemCarrito(alfombra, 5);
        carrito.getProductos().add(itemAlfombra);

        assertEquals(6750.0, carritoService.calculateTotalPrice(carrito));
    }
}
