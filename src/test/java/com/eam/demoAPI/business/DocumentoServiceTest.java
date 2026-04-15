package com.eam.demoAPI.business;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.service.impl.DocumentoServiceImpl;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.HistorialDocumentoDAO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentoService - Unit Tests")
public class DocumentoServiceTest {

    @Mock
    private DocumentoDAO documentoDAO;

    @Mock
    private HistorialDocumentoDAO historialDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private DocumentoServiceImpl documentoService;

    private DocumentoDTO validDocumento;
    private Long validId;

    @BeforeEach
    void setUp() {
        validId = 1L;

        validDocumento = new DocumentoDTO();
        validDocumento.setId(validId);
        validDocumento.setNombre("Documento Test");
        validDocumento.setUsuarioId(1L);
        validDocumento.setEstado(EstadoDocumento.CREADO);
        validDocumento.setEliminado(false);
        validDocumento.setCreatedAt(LocalDateTime.now());
    }

    //  CREATE

    @Test
    @DisplayName("CREATE - Documento válido debe crearse")
    void createDocumento_valid_shouldReturnCreated() {

        when(usuarioDAO.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

        when(documentoDAO.save(any(DocumentoDTO.class)))
                .thenReturn(validDocumento);

        DocumentoDTO result = documentoService.createDocumento(validDocumento);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Documento Test");

        verify(documentoDAO, times(1)).save(any(DocumentoDTO.class));
    }

    @Test
    @DisplayName("CREATE - Nombre null debe lanzar excepción")
    void createDocumento_nullName_shouldThrow() {

        validDocumento.setNombre(null);

        assertThatThrownBy(() -> documentoService.createDocumento(validDocumento))
                .isInstanceOf(IllegalArgumentException.class);

        verify(documentoDAO, never()).save(any());
    }

    //READ

    @Test
    @DisplayName("GET BY ID - Documento existe")
    void getDocumentoById_exists() {

        when(documentoDAO.findById(validId))
                .thenReturn(Optional.of(validDocumento));

        DocumentoDTO result = documentoService.getDocumentoById(validId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(validId);

        verify(documentoDAO, times(1)).findById(validId);
    }

    @Test
    @DisplayName("GET BY ID - Documento no existe")
    void getDocumentoById_notFound() {

        when(documentoDAO.findById(validId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.getDocumentoById(validId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Documento no encontrado");
    }

    // FILTER

    @Test
    @DisplayName("FILTER - Retorna lista de documentos")
    void getDocumentosFiltrados_ok() {

        when(documentoDAO.findAll())
                .thenReturn(List.of(validDocumento));

        List<DocumentoDTO> result = documentoService.getDocumentosFiltrados(null, null);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);

        verify(documentoDAO, times(1)).findAll();
    }

    // UPDATE
    @Test
    @DisplayName("UPDATE - Documento actualizado correctamente")
    void updateDocumento_ok() {

        DocumentoDTO update = new DocumentoDTO();
        update.setNombre("Nuevo nombre");

        when(documentoDAO.findById(validId))
                .thenReturn(Optional.of(validDocumento));

        when(documentoDAO.update(eq(validId), any()))
                .thenReturn(Optional.of(validDocumento));

        DocumentoDTO result = documentoService.updateDocumento(validId, update);

        assertThat(result).isNotNull();

        verify(documentoDAO, times(1)).update(eq(validId), any());
    }

    @Test
    @DisplayName("UPDATE - Documento no existe")
    void updateDocumento_notFound() {

        when(documentoDAO.findById(validId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.updateDocumento(validId, validDocumento))
                .isInstanceOf(RuntimeException.class);
    }

    //  DELETE (SOFT)

    @Test
    @DisplayName("SOFT DELETE - Documento eliminado lógicamente")
    void softDelete_ok() {

        when(documentoDAO.findById(validId))
                .thenReturn(Optional.of(validDocumento));

        when(documentoDAO.update(eq(validId), any()))
                .thenReturn(Optional.of(validDocumento));

        assertThatCode(() -> documentoService.softDeleteDocumento(validId))
                .doesNotThrowAnyException();
    }

    // PAPELERA
    @Test
    @DisplayName("PAPELERA - Retorna documentos eliminados")
    void getEliminados_ok() {

        validDocumento.setEliminado(true);

        when(documentoDAO.findEliminados())
                .thenReturn(List.of(validDocumento));

        List<DocumentoDTO> result = documentoService.getDocumentosEliminados();

        assertThat(result).isNotEmpty();
    }

    //RESTORE

    @Test
    @DisplayName("RESTORE - Documento restaurado")
    void restore_ok() {

        validDocumento.setEliminado(true); // estaba en papelera

        when(documentoDAO.findById(validId))
                .thenReturn(Optional.of(validDocumento));

        when(documentoDAO.update(eq(validId), any()))
                .thenReturn(Optional.of(validDocumento));

        assertThatCode(() -> documentoService.restoreDocumento(validId))
                .doesNotThrowAnyException();
    }

    //DELETE PERMANENTE

    @Test
    @DisplayName("DELETE - Eliminación permanente")
    void deletePermanent_ok() {

        when(documentoDAO.delete(validId))
                .thenReturn(true);

        assertThatCode(() -> documentoService.deletePermanent(validId))
                .doesNotThrowAnyException();

        verify(documentoDAO, times(1)).delete(validId);
    }

    @Test
    @DisplayName("DELETE - Documento no existe")
    void deletePermanent_notFound() {

        when(documentoDAO.delete(validId))
                .thenReturn(false);

        assertThatThrownBy(() -> documentoService.deletePermanent(validId))
                .isInstanceOf(RuntimeException.class);
    }
}