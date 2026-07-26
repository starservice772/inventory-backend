package com.starservice.inventory.inventory_app.dto.users;

import lombok.Data;

@Data
public class ForgotPasswordRequest {

    private String id;
    private String username;
    private String password;
}
