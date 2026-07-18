package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.OfficeStock;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficeStockRepository extends JpaRepository<OfficeStock, String> {

    Optional<OfficeStock> findByItemCodeAndDefaultCompany(String itemCode, Company defaultCompany);

    Page<OfficeStock> findByDefaultCompanyAndQuantityGreaterThan(
            Company defaultCompany, Integer quantity, Pageable pageable);
}
