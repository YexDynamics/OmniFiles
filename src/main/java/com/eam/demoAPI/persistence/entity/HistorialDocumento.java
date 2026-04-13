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

    private String accion;

    private String estadoResultante;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}