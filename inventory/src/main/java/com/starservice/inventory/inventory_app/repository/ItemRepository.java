package com.starservice.inventory.inventory_app.repository;


import com.starservice.inventory.inventory_app.entity.Item;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, String> {

    Optional<Item> findByItemCodeAndDelFlFalse(String itemCode);

    Optional<Item> findByIdAndDelFlFalse(String id);

    Optional<Item> findByItemCodeAndCompanyAndDelFlFalse(String itemCode, Company company);

    Optional<Item> findByIdAndCompanyAndDelFlFalse(String id, Company company);

//    Page<Item> findByDelFlFalse(Pageable pageable);

//    @Query("""
//            SELECT i FROM Item i
//            WHERE i.delFl = false
//            AND (
//                LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :searchKey, '%'))
//                OR LOWER(i.itemDescription) LIKE LOWER(CONCAT('%', :searchKey, '%'))
//            )
//            """)
//    Page<Item> searchItems(@Param("searchKey") String searchKey,
//                           Pageable pageable);

    @Query("""
       SELECT i.itemCode
       FROM Item i
       WHERE i.company = :company
       AND i.delFl = false
       """)
    List<String> findAllItemCodesByCompany(@Param("company") Company company);
}
