package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.auth.AuthRequest;
import com.starservice.inventory.inventory_app.dto.auth.AuthResponse;
import com.starservice.inventory.inventory_app.entity.User;
import com.starservice.inventory.inventory_app.repository.UserRepository;
import com.starservice.inventory.inventory_app.service.CustomUserDetailsService;
import com.starservice.inventory.inventory_app.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {

        User user = userRepository
                .findByUsernameAndCompany(request.getUsername(), request.getCompany())
                .orElseThrow(() -> new RuntimeException("Invalid username or company"));

        // manual password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getCompany());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .companyName(String.valueOf(user.getCompany()))
                .build();
    }
}