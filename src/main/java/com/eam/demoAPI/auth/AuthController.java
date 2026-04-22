package com.eam.demoAPI.auth;

import com.eam.demoAPI.auth.dto.LoginRequest;
import com.eam.demoAPI.business.dto.LoginResponseDTO;
import com.eam.demoAPI.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y gestión de tokens JWT")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Retorna token JWT y datos del usuario")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponseDTO response = authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }
}
