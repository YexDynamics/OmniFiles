package com.eam.demoAPI.business;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.dto.UsuarioResponseDTO;
import com.eam.demoAPI.business.service.impl.UsuarioServiceImpl;
import com.eam.demoAPI.exception.ConflictException;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Unit Tests")
public class UsuarioServiceTest {

    @Mock private UsuarioDAO usuarioDAO;
    @InjectMocks private UsuarioServiceImpl usuarioService;

    private UsuarioDTO validUsuario;
    private UsuarioResponseDTO validResponse;
    private Long validId;

    @BeforeEach
    void setUp() {
        validId = 1L;

        validUsuario = new UsuarioDTO();
        validUsuario.setId(validId);
        validUsuario.setNombre("Juan Pérez");
        validUsuario.setEmail("juan@test.com");
        validUsuario.setContrasena("pass123");
        validUsuario.setRolId(1L);
        validUsuario.setActivo(true);

        validResponse = new UsuarioResponseDTO();
        validResponse.setId(validId);
        validResponse.setNombre("Juan Pérez");
        validResponse.setEmail("juan@test.com");
        validResponse.setRolId(1L);
        validResponse.setActivo(true);
    }

    //  CREATE

    @Test @DisplayName("CREATE - Usuario válido debe crearse")
    void createUsuario_valid_shouldReturnCreated() {
        when(usuarioDAO.existsByEmail(anyString())).thenReturn(false);
        when(usuarioDAO.save(any(UsuarioDTO.class))).thenReturn(validResponse);

        UsuarioResponseDTO result = usuarioService.createUsuario(validUsuario);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Juan Pérez");
        assertThat(result.getActivo()).isTrue();
        verify(usuarioDAO, times(1)).save(any(UsuarioDTO.class));
    }

    @Test @DisplayName("CREATE - Email duplicado → 409 ConflictException")
    void createUsuario_duplicateEmail_shouldThrow() {
        when(usuarioDAO.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email ya está en uso");

        verify(usuarioDAO, never()).save(any());
    }

    @Test @DisplayName("CREATE - Nombre null → 400 IllegalArgument")
    void createUsuario_nullNombre_shouldThrow() {
        validUsuario.setNombre(null);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre es obligatorio");

        verify(usuarioDAO, never()).save(any());
    }

    @Test @DisplayName("CREATE - Email null → 400 IllegalArgument")
    void createUsuario_nullEmail_shouldThrow() {
        validUsuario.setEmail(null);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email es obligatorio");

        verify(usuarioDAO, never()).save(any());
    }

    @Test @DisplayName("CREATE - Rol null → 400 IllegalArgument")
    void createUsuario_nullRol_shouldThrow() {
        validUsuario.setRolId(null);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rol es obligatorio");

        verify(usuarioDAO, never()).save(any());
    }

    //READ

    @Test @DisplayName("GET BY ID - Usuario existe")
    void getUsuarioById_exists() {
        when(usuarioDAO.findResponseById(validId)).thenReturn(Optional.of(validResponse));

        UsuarioResponseDTO result = usuarioService.getUsuarioById(validId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(validId);
    }

    @Test @DisplayName("GET BY ID - Usuario no existe → NotFoundException")
    void getUsuarioById_notFound() {
        when(usuarioDAO.findResponseById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.getUsuarioById(validId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test @DisplayName("GET ALL - Retorna lista de usuarios")
    void getUsuarios_ok() {
        when(usuarioDAO.findAll()).thenReturn(List.of(validResponse));

        List<UsuarioResponseDTO> result = usuarioService.getUsuarios();

        assertThat(result).isNotEmpty().hasSize(1);
        verify(usuarioDAO, times(1)).findAll();
    }

    //UPDATE

    @Test @DisplayName("UPDATE - Usuario actualizado correctamente")
    void updateUsuario_ok() {
        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNombre("Nuevo Nombre");

        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.update(eq(validId), any())).thenReturn(Optional.of(validResponse));

        UsuarioResponseDTO result = usuarioService.updateUsuario(validId, cambios);

        assertThat(result).isNotNull();
        verify(usuarioDAO, times(1)).update(eq(validId), any());
    }

    @Test @DisplayName("UPDATE - Usuario no existe → NotFoundException")
    void updateUsuario_notFound() {
        when(usuarioDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.updateUsuario(validId, validUsuario))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // CAMBIAR ESTADO

    @Test @DisplayName("CAMBIAR ESTADO - Desactivar usuario")
    void cambiarEstado_inactivo() {
        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.update(eq(validId), any())).thenReturn(Optional.of(validResponse));

        usuarioService.cambiarEstado(validId, false);

        verify(usuarioDAO).update(eq(validId),
                argThat(u -> Boolean.FALSE.equals(u.getActivo())));
    }

    @Test @DisplayName("CAMBIAR ESTADO - Usuario no existe → NotFoundException")
    void cambiarEstado_notFound() {
        when(usuarioDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cambiarEstado(validId, false))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // DELETE

    @Test @DisplayName("DELETE - Usuario eliminado correctamente")
    void deleteUsuario_ok() {
        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.delete(validId)).thenReturn(true);

        assertThatCode(() -> usuarioService.deleteUsuario(validId))
                .doesNotThrowAnyException();

        verify(usuarioDAO, times(1)).delete(validId);
    }

    @Test @DisplayName("DELETE - Usuario no existe → NotFoundException")
    void deleteUsuario_notFound() {
        when(usuarioDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.deleteUsuario(validId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioDAO, never()).delete(any());
    }

    @Test @DisplayName("DELETE - Fallo interno al eliminar → RuntimeException")
    void deleteUsuario_deleteFails() {
        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.delete(validId)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.deleteUsuario(validId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo eliminar");
    }
}
