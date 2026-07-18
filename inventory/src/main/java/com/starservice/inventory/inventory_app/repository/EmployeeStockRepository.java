package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.EmployeeStock;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeStockRepository extends JpaRepository<EmployeeStock, String> {

    Optional<EmployeeStock> findByItemCodeAndEmployeeIdAndDefaultCompany(
            String itemCode, String employeeId, Company defaultCompany);

    List<EmployeeStock> findByItemCodeAndDefaultCompany(String itemCode, Company defaultCompany);

    Page<EmployeeStock> findByDefaultCompanyAndQuantityGreaterThan(
            Company defaultCompany, Integer quantity, Pageable pageable);
}
