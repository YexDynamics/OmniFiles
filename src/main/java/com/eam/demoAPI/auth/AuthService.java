package com.eam.demoAPI.auth;

import com.eam.demoAPI.business.dto.LoginResponseDTO;
import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.business.dto.UsuarioResponseDTO;
import com.eam.demoAPI.exception.NotFoundException;
import com.eam.demoAPI.persistence.dao.UsuarioDAO;
import com.eam.demoAPI.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponseDTO login(String email, String password) {

        UsuarioDTO user = usuarioDAO.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(user.getActivo())) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(password, user.getContrasena())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(email);

        UsuarioResponseDTO responseDTO = usuarioDAO.findResponseById(user.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return new LoginResponseDTO(token, responseDTO);
    }
}
