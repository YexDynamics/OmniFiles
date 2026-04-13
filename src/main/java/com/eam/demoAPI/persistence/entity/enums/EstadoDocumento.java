package com.eam.demoAPI.persistence.entity.enums;

public enum EstadoDocumento {
    CREADO("Creado"),
    EN_REVISION("En Revisión"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String nombreBonito;

    EstadoDocumento(String nombreBonito) {
        this.nombreBonito = nombreBonito;
    }

    public String getNombreBonito() {
        return nombreBonito;
    }
}
