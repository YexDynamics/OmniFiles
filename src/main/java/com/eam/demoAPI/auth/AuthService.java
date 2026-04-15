package com.eam.demoAPI.auth;

import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDTO register(String nombre, String email, String password) {

        UsuarioDTO user = new UsuarioDTO();
        user.setNombre(nombre);
        user.setEmail(email);
        user.setContrasena(passwordEncoder.encode(password));

        return usuarioDAO.save(user);
    }

    public UsuarioDTO login(String email, String password) {

        UsuarioDTO user = usuarioDAO.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, user.getContrasena())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return user;
    }
}
