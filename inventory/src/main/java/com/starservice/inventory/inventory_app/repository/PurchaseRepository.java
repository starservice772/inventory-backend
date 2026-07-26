package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.Purchase;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, String> {

    Page<Purchase> findByDefaultCompanyAndCreatedDateBetweenOrderByCreatedDateAsc(
            Company defaultCompany, Instant startDate, Instant endDate, Pageable pageable);
}
