package org.example;

public class ServicioNomina {

    public Boolean addHorasTrabajadas(Empleado empleado, int horasTrabajadas) {
        if (empleado.getTipoEmpleado() == TipoEmpleado.PART_TIME && empleado.getHorasTrabajadas() + horasTrabajadas > 40) {
            return false;
        }
        empleado.setHorasTrabajadas(empleado.getHorasTrabajadas() + horasTrabajadas);
        return true;
    }

    public double calcularSalario(Empleado empleado) {
        double salario = 0;
        if (empleado.getHorasTrabajadas() > 40){
            int horasExtra = empleado.getHorasTrabajadas() - 40;
            salario = 40 * empleado.getTarifaPorHora() + horasExtra * empleado.getTarifaPorHora() * 1.5;
        }else {
            salario = empleado.getHorasTrabajadas() * empleado.getTarifaPorHora();
        }
        return salario;
    }
}
