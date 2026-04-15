package com.eam.demoAPI.auth;

import com.eam.demoAPI.auth.dto.LoginRequest;
import com.eam.demoAPI.business.dto.UsuarioDTO;
import com.eam.demoAPI.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        UsuarioDTO created = authService.register(request.getNombre(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UsuarioDTO user = authService.login(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of("token", token, "usuario", user));
    }
}
