package com.eam.demoAPI.business;

import com.eam.demoAPI.business.dto.DocumentoDTO;
import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.service.impl.DocumentoServiceImpl;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.DocumentoDAO;
import com.eam.demoAPI.persistence.dao.HistorialDocumentoDAO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.persistence.entity.enums.EstadoDocumento;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentoService - Unit Tests")
public class DocumentoServiceTest {

    @Mock private DocumentoDAO documentoDAO;
    @Mock private HistorialDocumentoDAO historialDAO;
    @Mock private UsuarioDAO usuarioDAO;

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

        // Mock SecurityContext para registrarAccion()
        UsuarioDTO usuarioActual = new UsuarioDTO();
        usuarioActual.setId(1L);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("test@test.com");

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(usuarioDAO.findByEmail("test@test.com")).thenReturn(Optional.of(usuarioActual));
    }

    // CREATE

    @Test @DisplayName("CREATE - Documento válido debe crearse")
    void createDocumento_valid_shouldReturnCreated() {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);

        when(usuarioDAO.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(documentoDAO.save(any(DocumentoDTO.class))).thenReturn(validDocumento);

        DocumentoDTO result = documentoService.createDocumento(validDocumento);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Documento Test");
        verify(documentoDAO, times(1)).save(any(DocumentoDTO.class));
    }

    @Test @DisplayName("CREATE - Nombre null debe lanzar excepción")
    void createDocumento_nullName_shouldThrow() {
        validDocumento.setNombre(null);

        assertThatThrownBy(() -> documentoService.createDocumento(validDocumento))
                .isInstanceOf(IllegalArgumentException.class);

        verify(documentoDAO, never()).save(any());
    }

    @Test @DisplayName("CREATE - Usuario no existe debe lanzar NotFoundException")
    void createDocumento_userNotFound_shouldThrow() {
        when(usuarioDAO.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.createDocumento(validDocumento))
                .isInstanceOf(NotFoundException.class);

        verify(documentoDAO, never()).save(any());
    }

    // READ

    @Test @DisplayName("GET BY ID - Documento existe")
    void getDocumentoById_exists() {
        when(documentoDAO.findById(validId)).thenReturn(Optional.of(validDocumento));

        DocumentoDTO result = documentoService.getDocumentoById(validId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(validId);
    }

    @Test @DisplayName("GET BY ID - Documento no existe → NotFoundException")
    void getDocumentoById_notFound() {
        when(documentoDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.getDocumentoById(validId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Documento no encontrado");
    }

    // FILTER

    @Test @DisplayName("FILTER - Sin parámetros retorna todos los activos")
    void getDocumentosFiltrados_ok() {
        when(documentoDAO.findAll()).thenReturn(List.of(validDocumento));

        List<DocumentoDTO> result = documentoService.getDocumentosFiltrados(
                null, null, null, null, null);

        assertThat(result).isNotEmpty().hasSize(1);
        verify(documentoDAO, times(1)).findAll();
    }

    @Test @DisplayName("FILTER - Filtrar por usuarioId")
    void getDocumentosFiltrados_usuario() {
        when(documentoDAO.findByUsuario(1L)).thenReturn(List.of(validDocumento));

        List<DocumentoDTO> result = documentoService.getDocumentosFiltrados(
                null, 1L, null, null, null);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(doc -> doc.getUsuarioId().equals(1L));
        verify(documentoDAO, times(1)).findByUsuario(1L);
    }

    // UPDATE

    @Test @DisplayName("UPDATE - Actualización correcta")
    void updateDocumento_ok() {
        DocumentoDTO update = new DocumentoDTO();
        update.setNombre("Nuevo nombre");

        when(documentoDAO.findById(validId)).thenReturn(Optional.of(validDocumento));
        when(documentoDAO.update(eq(validId), any())).thenReturn(Optional.of(validDocumento));

        DocumentoDTO result = documentoService.updateDocumento(validId, update);

        assertThat(result).isNotNull();
        verify(documentoDAO, times(1)).update(eq(validId), any());
    }

    @Test @DisplayName("UPDATE - Documento no existe → NotFoundException")
    void updateDocumento_notFound() {
        when(documentoDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.updateDocumento(validId, validDocumento))
                .isInstanceOf(NotFoundException.class);
    }

    //SOFT DELETE

    @Test @DisplayName("SOFT DELETE - Documento eliminado lógicamente")
    void softDelete_ok() {
        when(documentoDAO.findById(validId)).thenReturn(Optional.of(validDocumento));
        when(documentoDAO.update(eq(validId), any())).thenReturn(Optional.of(validDocumento));

        documentoService.softDeleteDocumento(validId);

        verify(documentoDAO).update(eq(validId), argThat(doc -> doc.getEliminado()));
    }

    //PAPELERA

    @Test @DisplayName("PAPELERA - Retorna documentos eliminados")
    void getEliminados_ok() {
        validDocumento.setEliminado(true);
        when(documentoDAO.findEliminados()).thenReturn(List.of(validDocumento));

        List<DocumentoDTO> result = documentoService.getDocumentosEliminados();

        assertThat(result).isNotEmpty();
    }

    // RESTORE

    @Test @DisplayName("RESTORE - Documento restaurado")
    void restore_ok() {
        validDocumento.setEliminado(true);
        when(documentoDAO.findById(validId)).thenReturn(Optional.of(validDocumento));
        when(documentoDAO.update(eq(validId), any())).thenReturn(Optional.of(validDocumento));

        documentoService.restoreDocumento(validId);

        verify(documentoDAO).update(eq(validId), argThat(doc -> !doc.getEliminado()));
    }

    // DELETE PERMANENTE

    @Test @DisplayName("DELETE - Eliminación permanente exitosa")
    void deletePermanent_ok() {
        when(documentoDAO.findById(validId)).thenReturn(Optional.of(validDocumento));
        when(documentoDAO.delete(validId)).thenReturn(true);

        assertThatCode(() -> documentoService.deletePermanent(validId))
                .doesNotThrowAnyException();

        verify(documentoDAO, times(1)).delete(validId);
    }

    @Test @DisplayName("DELETE - Documento no existe → NotFoundException")
    void deletePermanent_notFound() {
        when(documentoDAO.findById(validId)).thenReturn(Optional.of(validDocumento));
        when(documentoDAO.delete(validId)).thenReturn(false);

        assertThatThrownBy(() -> documentoService.deletePermanent(validId))
                .isInstanceOf(NotFoundException.class);
    }
}
