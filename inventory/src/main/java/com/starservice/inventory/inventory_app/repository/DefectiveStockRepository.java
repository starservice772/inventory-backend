package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.DefectiveStock;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefectiveStockRepository extends JpaRepository<DefectiveStock, String> {

    Optional<DefectiveStock> findByItemCodeAndDefaultCompany(String itemCode, Company defaultCompany);

    Page<DefectiveStock> findByDefaultCompanyAndQuantityGreaterThan(
            Company defaultCompany, Integer quantity, Pageable pageable);

    @Query("""
                SELECT d FROM DefectiveStock d
                WHERE d.defaultCompany = :company
                AND d.quantity > 0
                AND LOWER(d.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%'))
            """)
    Page<DefectiveStock> searchDefectiveStocksByItemCode(
            @Param("company") Company company,
            @Param("itemCode") String itemCode,
            Pageable pageable);
}
