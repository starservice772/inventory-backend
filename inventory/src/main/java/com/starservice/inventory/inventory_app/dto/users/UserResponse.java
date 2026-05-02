package com.starservice.inventory.inventory_app.dto.users;

import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String uuid;
    private String username;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private Company company;
    private String status;
}
