package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.ItemEmployeeStockValue;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ItemEmployeeStockValueRepository extends JpaRepository<ItemEmployeeStockValue, String> {

    Optional<ItemEmployeeStockValue> findByItemCodeAndEmpIdAndDefaultCompany(
            String itemCode, String empId, Company defaultCompany);

    List<ItemEmployeeStockValue> findByItemCodeInAndDefaultCompany(
            List<String> itemCodes, Company defaultCompany);
}
