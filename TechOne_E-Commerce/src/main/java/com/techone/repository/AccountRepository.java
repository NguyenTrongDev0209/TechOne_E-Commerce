package com.techone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techone.model.Account;

public interface AccountRepository extends JpaRepository<Account, Integer>{
	Optional<Account> findByEmail(String email);
    Optional<Account> findByPhone(String phone);
    Optional<Account> findByEmailOrPhone(String email, String phone);
    List<Account> findByRole(Boolean role);
    
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Account a WHERE a.role = false AND " +
           "(:search IS NULL OR LOWER(a.fullname) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR a.phone LIKE CONCAT('%', :search, '%')) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(cast(:dateFrom as date) IS NULL OR a.createAt >= :dateFrom) AND " +
           "(cast(:dateTo as date) IS NULL OR a.createAt <= :dateTo) " +
           "ORDER BY a.createAt DESC")
    List<Account> findCustomersByFilters(
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("status") Integer status,
            @org.springframework.data.repository.query.Param("dateFrom") java.time.LocalDate dateFrom,
            @org.springframework.data.repository.query.Param("dateTo") java.time.LocalDate dateTo);
}
