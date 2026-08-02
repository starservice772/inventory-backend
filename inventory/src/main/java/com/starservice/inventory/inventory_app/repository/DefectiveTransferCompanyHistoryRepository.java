package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.DefectiveTransferCompanyHistory;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DefectiveTransferCompanyHistoryRepository extends JpaRepository<DefectiveTransferCompanyHistory, String> {

    @Query(value = """
            SELECT * FROM defective_transfer_company_history h
            WHERE h.default_company = :#{#company.name()}
              AND STR_TO_DATE(h.transfer_date, '%d-%m-%Y') >= :startDate
              AND STR_TO_DATE(h.transfer_date, '%d-%m-%Y') <= :endDate
            ORDER BY STR_TO_DATE(h.transfer_date, '%d-%m-%Y') ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM defective_transfer_company_history h
            WHERE h.default_company = :#{#company.name()}
              AND STR_TO_DATE(h.transfer_date, '%d-%m-%Y') >= :startDate
              AND STR_TO_DATE(h.transfer_date, '%d-%m-%Y') <= :endDate
            """,
            nativeQuery = true)
    Page<DefectiveTransferCompanyHistory> findByCompanyAndTransferDateRange(
            @Param("company") Company company,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
