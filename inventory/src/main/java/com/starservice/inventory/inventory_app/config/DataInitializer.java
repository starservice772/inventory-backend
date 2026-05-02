package com.starservice.inventory.inventory_app.config;

import com.starservice.inventory.inventory_app.entity.User;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.enums.UserRole;
import com.starservice.inventory.inventory_app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class DataInitializer {
//
//    @Bean
//    CommandLineRunner initUsers(UserRepository userRepository,
//                                PasswordEncoder passwordEncoder) {
//        return args -> {
//
//            if (userRepository.count() == 0) {
//
//                // ================= GODREJ USERS =================
//
//                User adminGodrej = User.builder()
//                        .username("admin")
//                        .password(passwordEncoder.encode("admin123"))
//                        .role(UserRole.ROLE_ADMIN)
//                        .customUserId(generateCustomUserId(Company.GODREJ, 1))
//                        .company(Company.GODREJ)
//                        .name("Admin Godrej")
//                        .email("admin@godrej.com")
//                        .phone("9999999999")
//                        .activeFl(true)
//                        .delFl(false)
//                        .createdDate(Instant.now())
//                        .updatedDate(Instant.now())
//                        .build();
//
//                User managerGodrej = User.builder()
//                        .username("manager")
//                        .password(passwordEncoder.encode("manager123"))
//                        .role(UserRole.ROLE_MANAGER)
//                        .customUserId(generateCustomUserId(Company.GODREJ, 2))
//                        .company(Company.GODREJ)
//                        .name("Manager Godrej")
//                        .email("manager@godrej.com")
//                        .phone("8888888888")
//                        .activeFl(true)
//                        .delFl(false)
//                        .createdDate(Instant.now())
//                        .updatedDate(Instant.now())
//                        .build();
//
//                User crmGodrej = User.builder()
//                        .username("crm")
//                        .password(passwordEncoder.encode("crm123"))
//                        .role(UserRole.ROLE_CRM)
//                        .customUserId(generateCustomUserId(Company.GODREJ, 3))
//                        .company(Company.GODREJ)
//                        .name("CRM Godrej")
//                        .email("crm@godrej.com")
//                        .phone("8888888887")
//                        .activeFl(true)
//                        .delFl(false)
//                        .createdDate(Instant.now())
//                        .updatedDate(Instant.now())
//                        .build();
//
//                // ================= AOSMITH USERS =================
//
//                User adminAOS = User.builder()
//                        .username("admin")
//                        .password(passwordEncoder.encode("admin123"))
//                        .role(UserRole.ROLE_ADMIN)
//                        .customUserId(generateCustomUserId(Company.AOSMITH, 1))
//                        .company(Company.AOSMITH)
//                        .name("Admin AOSmith")
//                        .email("admin@aosmith.com")
//                        .phone("7777777777")
//                        .activeFl(true)
//                        .delFl(false)
//                        .createdDate(Instant.now())
//                        .updatedDate(Instant.now())
//                        .build();
//
//                User crmAOS = User.builder()
//                        .username("crm")
//                        .password(passwordEncoder.encode("crm123"))
//                        .role(UserRole.ROLE_CRM)
//                        .customUserId(generateCustomUserId(Company.AOSMITH, 2))
//                        .company(Company.AOSMITH)
//                        .name("CRM AOSmith")
//                        .email("crm@aosmith.com")
//                        .phone("6666666666")
//                        .activeFl(true)
//                        .delFl(false)
//                        .createdDate(Instant.now())
//                        .updatedDate(Instant.now())
//                        .build();
//
//                User managerAOS = User.builder()
//                        .username("manager")
//                        .password(passwordEncoder.encode("manager123"))
//                        .role(UserRole.ROLE_MANAGER)
//                        .customUserId(generateCustomUserId(Company.AOSMITH, 3))
//                        .company(Company.AOSMITH)
//                        .name("Manager AOSmith")
//                        .email("manager@aosmith.com")
//                        .phone("8888888888")
//                        .activeFl(true)
//                        .delFl(false)
//                        .createdDate(Instant.now())
//                        .updatedDate(Instant.now())
//                        .build();
//
//                userRepository.save(adminGodrej);
//                userRepository.save(managerGodrej);
//                userRepository.save(crmGodrej);
//                userRepository.save(adminAOS);
//                userRepository.save(managerAOS);
//                userRepository.save(crmAOS);
//            }
//        };
//    }
//
//    private String generateCustomUserId(Company company, int number) {
//        String code = switch (company) {
//            case GODREJ -> "GOD";
//            case AOSMITH -> "AOS";
//        };
//        return String.format("USER-%s-%02d", code, number);
//    }
}