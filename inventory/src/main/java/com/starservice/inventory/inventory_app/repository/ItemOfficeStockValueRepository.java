package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.ItemOfficeStockValue;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ItemOfficeStockValueRepository extends JpaRepository<ItemOfficeStockValue, String> {

    Optional<ItemOfficeStockValue> findByItemCodeAndDefaultCompany(String itemCode, Company defaultCompany);

    List<ItemOfficeStockValue> findByItemCodeInAndDefaultCompany(List<String> itemCodes, Company defaultCompany);
}
