package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.auth.AuthRequest;
import com.starservice.inventory.inventory_app.dto.auth.AuthResponse;
import com.starservice.inventory.inventory_app.entity.User;
import com.starservice.inventory.inventory_app.repository.UserRepository;
import com.starservice.inventory.inventory_app.service.CustomUserDetailsService;
import com.starservice.inventory.inventory_app.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

        if (Boolean.FALSE.equals(user.getActiveFl())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Your account has been deactivated. Please contact admin.");
        }

        // manual password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getCompany());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getCustomUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .companyName(String.valueOf(user.getCompany()))
                .build();
    }
}