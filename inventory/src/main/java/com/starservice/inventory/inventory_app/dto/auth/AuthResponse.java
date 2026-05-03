package com.starservice.inventory.inventory_app.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String username;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String companyName;
}
