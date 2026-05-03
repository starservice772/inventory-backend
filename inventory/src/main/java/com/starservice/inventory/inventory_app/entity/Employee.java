package com.starservice.inventory.inventory_app.entity;

import com.starservice.inventory.inventory_app.enums.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"employee_code", "company"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "gender")
    private String gender;

    @Column(name = "role")
    private String role;

    @Column(name = "company")
    @Enumerated(EnumType.STRING)
    private Company company;

    @Column(name = "active_fl")
    private Boolean activeFl;

    @Column(name = "del_fl")
    private Boolean delFl;

    @Column(name = "crtd_dt")
    private Instant createdDate;

    @Column(name = "updt_dt")
    private Instant updatedDate;
}
