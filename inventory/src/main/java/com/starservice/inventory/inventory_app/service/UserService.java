package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.users.UpdateUserRequest;
import com.starservice.inventory.inventory_app.dto.users.UserResponse;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.dto.users.AddUserRequest;
import com.starservice.inventory.inventory_app.entity.User;
import com.starservice.inventory.inventory_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse createUser(AddUserRequest request) {

        // Get logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userRepository
                .findByUsernameAndCompany(username, getCompanyFromToken())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ADMIN allowed
        if (!loggedInUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Only ADMIN can create users");
        }

        // Validate company
        if (!request.getCompany().equals(loggedInUser.getCompany())) {
            throw new RuntimeException("You cannot create user for another company");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        Optional<User> existingUser = userRepository
                .findByUsernameAndCompany(request.getUsername(), request.getCompany());

        if (existingUser.isPresent()) {
            throw new RuntimeException("Username already exists in this company");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .company(request.getCompany())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .activeFl(true)
                .delFl(false)
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .build();

        userRepository.save(user);

        // Return full response
        return UserResponse.builder()
                .uuid(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .company(user.getCompany())
                .status(Boolean.TRUE.equals(user.getActiveFl()) ? "ACTIVE" : "INACTIVE")
                .build();
    }

    // Extract company from JWT (better to move later to util)
    private Company getCompanyFromToken() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        // Minimal approach (since JWT already validated)
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        return user.getCompany();
    }

    public PageResponse<UserResponse> getUsers(int pageNo, int pageSize, String search) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Company company = loggedInUser.getCompany();

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
        Page<User> userPage;

        if (search == null || search.isBlank()) {
            userPage = userRepository.findByCompanyAndDelFlFalse(company, pageable);
        } else {
            userPage = userRepository.searchUsers(company, search, pageable);
        }

        List<UserResponse> users = userPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .totalPages(userPage.getTotalPages())
                .totalRecords(userPage.getTotalElements())
                .response(users)
                .build();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .uuid(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .company(user.getCompany())
                .status(Boolean.TRUE.equals(user.getActiveFl()) ? "ACTIVE" : "INACTIVE")
                .build();
    }

    public UserResponse getUserById(String id) {

        // Get logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Company company = loggedInUser.getCompany();

        // Fetch user by id + company + not deleted
        User user = userRepository.findByIdAndCompanyAndDelFlFalse(id, company)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    public String toggleUserStatus(String id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Only ADMIN
        if (!loggedInUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Only ADMIN can change status");
        }

        Company company = loggedInUser.getCompany();

        User user = userRepository.findByIdAndCompanyAndDelFlFalse(id, company).orElseThrow(() -> new RuntimeException("User not found"));

        user.setActiveFl(!Boolean.TRUE.equals(user.getActiveFl()));
        user.setUpdatedDate(Instant.now());

        userRepository.save(user);

        return user.getActiveFl() ? "User activated" : "User deactivated";
    }

    public String deleteUser(String id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Only ADMIN
        if (!loggedInUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Only ADMIN can delete users");
        }

        Company company = loggedInUser.getCompany();

        User user = userRepository.findByIdAndCompanyAndDelFlFalse(id, company).orElseThrow(() -> new RuntimeException("User not found"));

        user.setDelFl(true);
        user.setUpdatedDate(Instant.now());

        userRepository.save(user);

        return "User deleted successfully";
    }

    public UserResponse updateUser(UpdateUserRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Only ADMIN
        if (!loggedInUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Only ADMIN can update users");
        }

        Company company = loggedInUser.getCompany();

        User user = userRepository.findByIdAndCompanyAndDelFlFalse(request.getId(), company)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update allowed fields only
        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        user.setUpdatedDate(Instant.now());
        userRepository.save(user);
        return mapToResponse(user);
    }
}