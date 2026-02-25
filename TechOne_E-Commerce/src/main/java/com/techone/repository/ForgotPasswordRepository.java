package com.techone.repository;

import com.techone.model.ForgotPassword;
import com.techone.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {
    
    @Query("SELECT f FROM ForgotPassword f WHERE f.otp = ?1 AND f.account = ?2")
    Optional<ForgotPassword> findByOtpAndAccount(String otp, Account account);
    
    Optional<ForgotPassword> findByAccount(Account account);

    void deleteByAccount(Account account);
}
