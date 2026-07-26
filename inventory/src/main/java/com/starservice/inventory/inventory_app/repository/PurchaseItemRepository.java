package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, String> {

    List<PurchaseItem> findByPurchaseId(String purchaseId);

    List<PurchaseItem> findByPurchaseIdIn(List<String> purchaseIds);
}
