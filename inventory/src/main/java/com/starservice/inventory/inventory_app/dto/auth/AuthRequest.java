package com.starservice.inventory.inventory_app.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    private Company company;
}
