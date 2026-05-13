package com.eam.demoAPI.auth;

import com.eam.demoAPI.auth.dto.AuthRequest;
import com.eam.demoAPI.auth.dto.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
}
