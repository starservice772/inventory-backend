package com.starservice.inventory.inventory_app.dto.users;

import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.enums.UserRole;
import lombok.Data;

@Data
public class AddUserRequest {

    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private Company company;
}
