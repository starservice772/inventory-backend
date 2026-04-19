package com.starservice.inventory.inventory.repository;

import com.starservice.inventory.inventory.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, String> {
}
