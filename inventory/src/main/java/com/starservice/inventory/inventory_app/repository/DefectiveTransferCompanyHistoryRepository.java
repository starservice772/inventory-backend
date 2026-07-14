package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.DefectiveTransferCompanyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefectiveTransferCompanyHistoryRepository extends JpaRepository<DefectiveTransferCompanyHistory, String> {
}
