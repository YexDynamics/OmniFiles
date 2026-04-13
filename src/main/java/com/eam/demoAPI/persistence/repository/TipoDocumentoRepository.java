package com.eam.demoAPI.persistence.repository;

import com.eam.demoAPI.persistence.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

}
