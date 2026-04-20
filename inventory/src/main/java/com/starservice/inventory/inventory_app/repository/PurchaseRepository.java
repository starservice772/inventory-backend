package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, String> {
}
