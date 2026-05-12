package com.eam.demoAPI.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etapa_flujo",
       uniqueConstraints = @UniqueConstraint(columnNames = {"flujo_id", "orden"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtapaFlujo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flujo_id", nullable = false)
    private Flujo flujo;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // NULL = cualquier usuario con ese rol puede resolver
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
