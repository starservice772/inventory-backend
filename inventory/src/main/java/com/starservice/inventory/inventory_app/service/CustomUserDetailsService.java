package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.entity.User;
import com.starservice.inventory.inventory_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // We don’t support login without company
        throw new UsernameNotFoundException("Use company-based login");
    }

    public UserDetails loadUserByUsernameAndCompany(String username, Company company) {

        User user = userRepository
                .findByUsernameAndCompany(username, company)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}