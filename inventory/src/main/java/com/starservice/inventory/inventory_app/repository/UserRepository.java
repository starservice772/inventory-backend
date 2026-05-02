package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndCompany(String username, Company company);

    Page<User> findByCompanyAndDelFlFalse(Company company, Pageable pageable);

    Optional<User> findByIdAndCompanyAndDelFlFalse(String id, Company company);

    Optional<User> findTopByCompanyOrderByCustomUserIdDesc(Company company);

    @Query("""
                SELECT u FROM User u
                WHERE u.company = :company
                  AND u.delFl = false
                  AND (
                        LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<User> searchUsers(@Param("company") Company company,
            @Param("search") String search, Pageable pageable);

}
