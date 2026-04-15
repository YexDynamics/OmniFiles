package com.eam.demoAPI.persistence.entity.enums;

import lombok.Getter;

@Getter
public enum TipoAccion {

    CREACION("Creación"),
    ACTUALIZACION("Actualización"),
    CAMBIO_ESTADO("Cambio de estado"),
    ELIMINACION("Eliminación lógica"),
    RESTAURACION("Restauración"),
    DESCARGA("Descarga");

    private final String nombreBonito;

    TipoAccion(String nombreBonito) {
        this.nombreBonito = nombreBonito;
    }

}