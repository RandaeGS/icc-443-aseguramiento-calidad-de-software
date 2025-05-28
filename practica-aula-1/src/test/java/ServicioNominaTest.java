import org.example.Empleado;
import org.example.ServicioNomina;
import org.example.TipoEmpleado;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class ServicioNominaTest {

    Empleado empleadoPartTime;
    Empleado empleadoFullTime;
    ServicioNomina servicioNomina = new ServicioNomina();

    @BeforeEach
    void setUp() {
        empleadoPartTime = new Empleado();
        empleadoPartTime.setTipoEmpleado(TipoEmpleado.PART_TIME);

        empleadoFullTime = new Empleado();
        empleadoFullTime.setTipoEmpleado(TipoEmpleado.FULL_TIME);
    }

    @Test
    void ValidarHorasTrabajadasNotNegativas(){
        Assertions.assertTrue(true);
    }

    /**
     * Valida que la funcionalidad para add horas extra a un empleado no funcione si el empleado es part time.
     */
    @Test
    void ValidarNoHorasExtraAEmpleadosPartTime(){
        // Anadir horas por debajo de las 40
        empleadoPartTime.setHorasTrabajadas(10);
        Assertions.assertTrue(servicioNomina.addHorasTrabajadas(empleadoPartTime, 10));

        // Anadir horas por encima de las 40
        Assertions.assertFalse(servicioNomina.addHorasTrabajadas(empleadoPartTime, 40));
    }

    /**
     * Validar que el salario calculado sea correcto para empleados full time y part time
     */
    @Test
    void validarCalcularSalario(){
        empleadoPartTime.setHorasTrabajadas(20);
        empleadoPartTime.setTarifaPorHora(150.00);
        assertEquals(3000.00, servicioNomina.calcularSalario(empleadoPartTime), 0.001);

        empleadoFullTime.setHorasTrabajadas(40);
        empleadoFullTime.setTarifaPorHora(250.00);
        assertEquals(10000.00, servicioNomina.calcularSalario(empleadoFullTime), 0.001);

        empleadoFullTime.setHorasTrabajadas(50);
        assertEquals(13750.00, servicioNomina.calcularSalario(empleadoFullTime), 0.001);
    }
}
