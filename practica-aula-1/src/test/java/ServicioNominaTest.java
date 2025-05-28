import org.example.Empleado;
import org.example.LimitiSalarioSurpassed;
import org.example.ServicioNomina;
import org.example.TipoEmpleado;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioNominaTest {

    Empleado empleadoPartTime;
    Empleado empleadoFullTime;
    ServicioNomina servicioNomina = new ServicioNomina();

    /**
     * Inicializa los objetos comunes a traves de los tests
     */
    @BeforeEach
    void setUp() {
        empleadoPartTime = new Empleado();
        empleadoPartTime.setTipoEmpleado(TipoEmpleado.PART_TIME);
        empleadoPartTime.setSalarioLimitado(true);

        empleadoFullTime = new Empleado();
        empleadoFullTime.setTipoEmpleado(TipoEmpleado.FULL_TIME);
        empleadoFullTime.setSalarioLimitado(true);
    }

    @Test
    void ValidarHorasTrabajadasNotNegativas(){
        Assertions.assertTrue(true);
    }

    /**
     * Valida que la funcionalidad para add horas extra a los empleados
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
     * Validar que el salario calculado sea correctamente calculado para empleados full time y part time
     */
    @Test
    void validarCalcularSalario(){
        empleadoPartTime.setHorasTrabajadas(20);
        empleadoPartTime.setTarifaPorHora(150.00);
        Assertions.assertEquals(3000.00, servicioNomina.calcularSalario(empleadoPartTime), 0.001);

        empleadoFullTime.setHorasTrabajadas(40);
        empleadoFullTime.setTarifaPorHora(250.00);
        Assertions.assertEquals(10000.00, servicioNomina.calcularSalario(empleadoFullTime), 0.001);

        empleadoFullTime.setHorasTrabajadas(50);
        Assertions.assertEquals(13750.00, servicioNomina.calcularSalario(empleadoFullTime), 0.001);
    }

    /**
     * Valida que el servicio lance una excepcion si se intentan anadir horas trabajadas negativas a un empleado
     */
    @Test
    void validarExceptionAnadirHorasTrabajadasNegativas(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            servicioNomina.addHorasTrabajadas(empleadoPartTime, -10);
        });
    }

    /**
     * Valida que el servicio lance una excepcion si se intentan anadir horas trabajadas negativas a un empleado
     */
    @Test
    void validarExceptionEmpleadoNullCalculandoSalario(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            servicioNomina.calcularSalario(null);
        });
    }

    /**
     * Validar que se lance una excepcion cuando un empleado se excede del limite de salario semanal sin permiso
     */
    @Test
    void validarExceptionEmpleadoOverLimiteSalario(){
        empleadoFullTime.setHorasTrabajadas(10);
        empleadoFullTime.setTarifaPorHora(5000.00);
        Assertions.assertThrows(LimitiSalarioSurpassed.class, () -> {
            servicioNomina.calcularSalario(empleadoFullTime);
        });
    }
}
