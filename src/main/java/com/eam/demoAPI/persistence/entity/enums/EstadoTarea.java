package com.eam.demoAPI.persistence.entity.enums;

import lombok.Getter;

@Getter
public enum EstadoTarea {
    PENDIENTE("Pendiente"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    CORRECCION("Solicitud de corrección");

    private final String nombreBonito;

    EstadoTarea(String nombreBonito) { this.nombreBonito = nombreBonito; }
}
