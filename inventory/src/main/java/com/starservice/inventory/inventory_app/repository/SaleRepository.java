package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.Sale;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SaleRepository extends JpaRepository<Sale, String> {
    Page<Sale> findByDefaultCompanyAndCreatedDateBetweenOrderByCreatedDateAsc(
            Company defaultCompany, Instant startDate, Instant endDate, Pageable pageable);
}
