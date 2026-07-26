package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.DefectiveTransferCompanyHistory;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface DefectiveTransferCompanyHistoryRepository extends JpaRepository<DefectiveTransferCompanyHistory, String> {
    Page<DefectiveTransferCompanyHistory> findByDefaultCompanyAndTransferDateBetweenOrderByTransferDateAsc(
            Company defaultCompany, Instant startDate, Instant endDate, Pageable pageable);
}
