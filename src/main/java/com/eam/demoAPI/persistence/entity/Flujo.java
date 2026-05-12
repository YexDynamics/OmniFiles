package com.eam.demoAPI.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "flujo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flujo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "flujo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<EtapaFlujo> etapas;
}
