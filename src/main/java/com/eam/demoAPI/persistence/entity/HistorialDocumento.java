package com.eam.demoAPI.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estado_id")
    private EstadoDocumento estado;

    @ManyToOne
    @JoinColumn(name = "accion_id")
    private TipoAccion accion;

    private LocalDateTime fechaCambio;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
