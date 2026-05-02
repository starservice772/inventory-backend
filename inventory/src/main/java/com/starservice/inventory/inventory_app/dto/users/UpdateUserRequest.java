package com.starservice.inventory.inventory_app.dto.users;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String id;
    private String name;
    private String email;
    private String phone;
}
