package com.eam.demoAPI.business.service;

import com.eam.demoAPI.business.dto.TipoDocumentoDTO;

import java.util.List;

public interface TipoDocumentoService {

    TipoDocumentoDTO createTipoDocumento(TipoDocumentoDTO dto);

    TipoDocumentoDTO getTipoDocumentoById(Long id);

    List<TipoDocumentoDTO> getTiposDocumentales();

    List<TipoDocumentoDTO> getTiposActivos();

    TipoDocumentoDTO updateTipoDocumento(Long id, TipoDocumentoDTO dto);

    void cambiarEstado(Long id, boolean estado);
}
