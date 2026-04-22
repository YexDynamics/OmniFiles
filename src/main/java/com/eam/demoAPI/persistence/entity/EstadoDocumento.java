package com.eam.demoAPI.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    private String descripcion;
}
