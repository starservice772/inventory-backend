package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.dto.auth.Company;
import com.starservice.inventory.inventory_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndCompany(String username, Company company);
}
