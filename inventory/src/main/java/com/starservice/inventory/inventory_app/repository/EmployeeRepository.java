package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.Employee;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Optional<Employee> findByEmployeeCodeAndCompany(String employeeCode, Company company);

    Optional<Employee> findByIdAndCompanyAndDelFlFalse(String id, Company company);

    Page<Employee> findByCompanyAndDelFlFalse(Company company, Pageable pageable);

    @Query("""
                SELECT e FROM Employee e
                WHERE e.company = :company
                AND e.delFl = false
                AND (
                    LOWER(e.name) LIKE LOWER(CONCAT('%', :searchKey, '%'))
                    OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :searchKey, '%'))
                    OR LOWER(e.role) LIKE LOWER(CONCAT('%', :searchKey, '%'))
                    OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :searchKey, '%'))
                )
            """)
    Page<Employee> searchEmployees(@Param("company") Company company,
            @Param("searchKey") String searchKey, Pageable pageable);

    List<Employee> findByCompanyAndDelFlFalseAndActiveFlTrueOrderByNameAsc(Company company);

    @Query("""
                SELECT e FROM Employee e
                WHERE e.company = :company
                AND e.delFl = false
                AND e.activeFl = true
                AND LOWER(e.name) LIKE LOWER(CONCAT('%', :searchKey, '%'))
                ORDER BY e.name ASC
            """)
    List<Employee> searchEmployeesByName(@Param("company") Company company,
            @Param("searchKey") String searchKey);
}
