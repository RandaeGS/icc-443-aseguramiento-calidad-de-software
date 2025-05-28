package org.example;

import lombok.Data;

@Data
public class Empleado {
    private int horasTrabajadas;
    private Double tarifaPorHora;
    private Boolean salarioLimitado;
    private TipoEmpleado tipoEmpleado;
}
