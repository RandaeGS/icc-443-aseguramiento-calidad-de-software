import org.example.Empleado;
import org.example.ServicioNomina;
import org.example.TipoEmpleado;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

class ServicioNominaTest {

    Empleado empleadoPartTime;
    Empleado empleadoFullTime;
    ServicioNomina servicioNomina;

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
        servicioNomina = new ServicioNomina();
        Assertions.assertFalse(servicioNomina.addHorasTrabajadas(empleadoPartTime, 10));
    }
}
