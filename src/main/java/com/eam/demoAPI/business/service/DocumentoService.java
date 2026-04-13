package com.eam.demoAPI.business.service;


import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.dto.HistorialDocumentoDTO;

import java.util.List;

public interface DocumentoService {

    DocumentoDTO createDocumento(DocumentoDTO documentoDTO);

    DocumentoDTO getDocumentoById(Long id);

    List<DocumentoDTO> getDocumentosFiltrados(String estado, Long usuarioId);

    DocumentoDTO updateDocumento(Long id, DocumentoDTO documentoDTO);

    void softDeleteDocumento(Long id);

    List<DocumentoDTO> getDocumentosEliminados();

    void restoreDocumento(Long id);

    void deletePermanent(Long id);

    byte[] downloadDocumento(Long id);

    List<HistorialDocumentoDTO> getHistorial(Long documentoId);
}