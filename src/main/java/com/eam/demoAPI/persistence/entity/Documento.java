package com.eam.demoAPI.persistence.entity;

import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String rutaArchivo;

    @Enumerated(EnumType.STRING)
    private EstadoDocumento estado;

    private LocalDateTime fechaCreacion;
    private Boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "tipo_documento_id")
    private TipoDocumento tipoDocumento;
}