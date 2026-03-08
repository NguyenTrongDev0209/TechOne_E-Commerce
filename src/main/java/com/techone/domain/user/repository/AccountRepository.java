package com.techone.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.techone.domain.user.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByPhone(String phone);

    Optional<Account> findByEmailOrPhone(String email, String phone);

    List<Account> findByRole(Boolean role);

    @Query("SELECT a FROM Account a WHERE a.role = false AND " +
            "(:search IS NULL OR LOWER(a.fullname) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR a.phone LIKE CONCAT('%', :search, '%')) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(cast(:dateFrom as date) IS NULL OR a.createAt >= :dateFrom) AND " +
            "(cast(:dateTo as date) IS NULL OR a.createAt <= :dateTo) " +
            "ORDER BY a.createAt DESC")
    Page<Account> findCustomersByFilters(
            @Param("search") String search,
            @Param("status") Integer status,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo,
            Pageable pageable);

    long countByRole(Boolean role);

    long countByRoleAndStatus(Boolean role, Integer status);
}
