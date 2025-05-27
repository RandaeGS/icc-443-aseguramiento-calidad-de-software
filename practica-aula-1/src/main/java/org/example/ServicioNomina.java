package org.example;

public class ServicioNomina {

    public Boolean addHorasTrabajadas(Empleado empleado, int horasTrabajadas) {
        if (empleado.getTipoEmpleado() == TipoEmpleado.PART_TIME){
            return false;
        }
        empleado.setHorasTrabajadas(empleado.getHorasTrabajadas() + horasTrabajadas);
        return true;
    }
}
