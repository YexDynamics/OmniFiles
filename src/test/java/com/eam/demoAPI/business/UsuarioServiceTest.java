package com.eam.demoAPI.business;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.service.impl.UsuarioServiceImpl;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Unit Tests")
public class UsuarioServiceTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioDTO validUsuario;
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
    }

    //CREATE

    @Test
    @DisplayName("CREATE - Usuario válido debe crearse")
    void createUsuario_valid_shouldReturnCreated() {

        when(usuarioDAO.existsByEmail(anyString())).thenReturn(false);
        when(usuarioDAO.save(any(UsuarioDTO.class))).thenReturn(validUsuario);

        UsuarioDTO result = usuarioService.createUsuario(validUsuario);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Juan Pérez");
        assertThat(result.getActivo()).isTrue();

        verify(usuarioDAO, times(1)).save(any(UsuarioDTO.class));
    }

    @Test
    @DisplayName("CREATE - Email duplicado debe lanzar excepción")
    void createUsuario_duplicateEmail_shouldThrow() {

        when(usuarioDAO.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email ya está en uso");

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Nombre null debe lanzar excepción")
    void createUsuario_nullNombre_shouldThrow() {

        validUsuario.setNombre(null);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre es obligatorio");

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Email null debe lanzar excepción")
    void createUsuario_nullEmail_shouldThrow() {

        validUsuario.setEmail(null);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email es obligatorio");

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Rol null debe lanzar excepción")
    void createUsuario_nullRol_shouldThrow() {

        validUsuario.setRolId(null);

        assertThatThrownBy(() -> usuarioService.createUsuario(validUsuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rol es obligatorio");

        verify(usuarioDAO, never()).save(any());
    }

    //READ

    @Test
    @DisplayName("GET BY ID - Usuario existe")
    void getUsuarioById_exists() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));

        UsuarioDTO result = usuarioService.getUsuarioById(validId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(validId);

        verify(usuarioDAO, times(1)).findById(validId);
    }

    @Test
    @DisplayName("GET BY ID - Usuario no existe")
    void getUsuarioById_notFound() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.getUsuarioById(validId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("GET ALL - Retorna lista de usuarios")
    void getUsuarios_ok() {

        when(usuarioDAO.findAll()).thenReturn(List.of(validUsuario));

        List<UsuarioDTO> result = usuarioService.getUsuarios();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);

        verify(usuarioDAO, times(1)).findAll();
    }

    //UPDATE

    @Test
    @DisplayName("UPDATE - Usuario actualizado correctamente")
    void updateUsuario_ok() {

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNombre("Nuevo Nombre");

        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.update(eq(validId), any())).thenReturn(Optional.of(validUsuario));

        UsuarioDTO result = usuarioService.updateUsuario(validId, cambios);

        assertThat(result).isNotNull();

        verify(usuarioDAO, times(1)).update(eq(validId), any());
    }

    @Test
    @DisplayName("UPDATE - Usuario no existe")
    void updateUsuario_notFound() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.updateUsuario(validId, validUsuario))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    //CAMBIAR ESTADO

    @Test
    @DisplayName("CAMBIAR ESTADO - Desactivar usuario")
    void cambiarEstado_inactivo() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.update(eq(validId), any())).thenReturn(Optional.of(validUsuario));

        usuarioService.cambiarEstado(validId, false);

        verify(usuarioDAO).update(eq(validId),
                argThat(u -> Boolean.FALSE.equals(u.getActivo())));
    }

    @Test
    @DisplayName("CAMBIAR ESTADO - Usuario no existe")
    void cambiarEstado_notFound() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cambiarEstado(validId, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    //DELETE

    @Test
    @DisplayName("DELETE - Usuario eliminado correctamente")
    void deleteUsuario_ok() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.delete(validId)).thenReturn(true);

        assertThatCode(() -> usuarioService.deleteUsuario(validId))
                .doesNotThrowAnyException();

        verify(usuarioDAO, times(1)).delete(validId);
    }

    @Test
    @DisplayName("DELETE - Usuario no existe debe lanzar excepción")
    void deleteUsuario_notFound() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.deleteUsuario(validId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioDAO, never()).delete(any());
    }

    @Test
    @DisplayName("DELETE - Fallo al eliminar debe lanzar excepción")
    void deleteUsuario_deleteFails() {

        when(usuarioDAO.findById(validId)).thenReturn(Optional.of(validUsuario));
        when(usuarioDAO.delete(validId)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.deleteUsuario(validId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo eliminar");
    }
}
