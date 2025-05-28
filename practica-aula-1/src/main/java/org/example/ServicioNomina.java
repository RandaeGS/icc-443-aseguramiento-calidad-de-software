package org.example;

public class ServicioNomina {

    public Boolean addHorasTrabajadas(Empleado empleado, int horasTrabajadas) {
        if (horasTrabajadas < 1) {
            throw new IllegalArgumentException("Horas trabajadas no puede ser menor que 1");
        }

        if (empleado.getTipoEmpleado() == TipoEmpleado.PART_TIME && empleado.getHorasTrabajadas() + horasTrabajadas > 40) {
            return false;
        }
        empleado.setHorasTrabajadas(empleado.getHorasTrabajadas() + horasTrabajadas);
        return true;
    }

    public double calcularSalario(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("Empleado no puede ser nulo");
        }

        double salario = 0;
        if (empleado.getHorasTrabajadas() > 40){
            int horasExtra = empleado.getHorasTrabajadas() - 40;
            salario = 40 * empleado.getTarifaPorHora() + horasExtra * empleado.getTarifaPorHora() * 1.5;
        }else {
            salario = empleado.getHorasTrabajadas() * empleado.getTarifaPorHora();
        }

        if (empleado.getSalarioLimitado() && salario > 20000){
            throw new LimitiSalarioSurpassed("Empleado no tiene permitido ganar mas de 20,000 semanal");
        }

        return salario;
    }
}
