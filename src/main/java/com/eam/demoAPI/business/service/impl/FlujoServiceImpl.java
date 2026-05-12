package com.eam.demoAPI.business.service.impl;

import com.eam.demoAPI.business.dto.EtapaFlujoDTO;
import com.eam.demoAPI.business.dto.FlujoDTO;
import com.eam.demoAPI.business.service.FlujoService;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.FlujoDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FlujoServiceImpl implements FlujoService {

    private final FlujoDAO flujoDAO;

    @Override
    public FlujoDTO crearFlujo(FlujoDTO dto) {
        log.info("Creando flujo para tipo documento ID: {}", dto.getTipoDocumentoId());
        return flujoDAO.crearFlujo(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public FlujoDTO getFlujoByTipoDocumento(Long tipoDocumentoId) {
        return flujoDAO.findByTipoDocumentoId(tipoDocumentoId)
                .orElseThrow(() -> new NotFoundException("La plantilla no tiene flujo configurado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlujoDTO> getFlujos() {
        return flujoDAO.findAll();
    }

    @Override
    public EtapaFlujoDTO agregarEtapa(Long flujoId, EtapaFlujoDTO dto) {
        log.info("Agregando etapa orden {} al flujo ID: {}", dto.getOrden(), flujoId);
        return flujoDAO.agregarEtapa(flujoId, dto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaFlujoDTO> getEtapasByFlujo(Long flujoId) {
        return flujoDAO.getEtapasByFlujo(flujoId);
    }

    @Override
    public void eliminarEtapa(Long etapaId) {
        log.info("Eliminando etapa ID: {}", etapaId);
        flujoDAO.eliminarEtapa(etapaId);
    }
}
