package com.eam.demoAPI.business.service;

import com.eam.demoAPI.business.dto.EtapaFlujoDTO;
import com.eam.demoAPI.business.dto.FlujoDTO;

import java.util.List;

public interface FlujoService {

    FlujoDTO crearFlujo(FlujoDTO dto);

    FlujoDTO getFlujoByTipoDocumento(Long tipoDocumentoId);

    List<FlujoDTO> getFlujos();

    EtapaFlujoDTO agregarEtapa(Long flujoId, EtapaFlujoDTO dto);

    List<EtapaFlujoDTO> getEtapasByFlujo(Long flujoId);

    void eliminarEtapa(Long etapaId);
}
