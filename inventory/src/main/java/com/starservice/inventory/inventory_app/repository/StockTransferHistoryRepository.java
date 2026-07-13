package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.StockTransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransferHistoryRepository extends JpaRepository<StockTransferHistory, String> {
}
