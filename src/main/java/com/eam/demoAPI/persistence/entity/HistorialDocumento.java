package com.eam.demoAPI.persistence.entity;

import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import com.eam.demoAPI.persistence.entity.enums.TipoAccion;
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

    @Enumerated(EnumType.STRING)
    private EstadoDocumento estado;

    @Enumerated(EnumType.STRING)
    private TipoAccion accion;

    private LocalDateTime fechaCambio;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}