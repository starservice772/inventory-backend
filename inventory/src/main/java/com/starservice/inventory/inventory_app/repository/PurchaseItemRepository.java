package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, String> {

    List<PurchaseItem> findByPurchaseId(String purchaseId);
}