package com.eam.demoAPI.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private Long id;
    private String nombre;
    private String email;
    private String password;

}