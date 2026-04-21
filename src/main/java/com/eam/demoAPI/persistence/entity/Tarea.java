package com.eam.demoAPI.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarea")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @ManyToOne
    @JoinColumn(name = "usuario_asignado_id", nullable = false)
    private Usuario usuarioAsignado;

    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoTarea estado;

    @Column(length = 500)
    private String observaciones;

    private LocalDateTime fechaAsignacion;

    private LocalDateTime fechaResolucion;
}
