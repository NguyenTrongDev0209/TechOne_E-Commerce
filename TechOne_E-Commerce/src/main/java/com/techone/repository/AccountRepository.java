package com.techone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techone.model.Account;

public interface AccountRepository extends JpaRepository<Account, Integer>{
	Optional<Account> findByEmail(String email);
    Optional<Account> findByPhone(String phone);
    Optional<Account> findByEmailOrPhone(String email, String phone);
}
