package com.techone.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.Account;
import com.techone.model.ForgotPassword;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {
    Optional<ForgotPassword> findByOtpAndAccount(String otp, Account account);
    Optional<ForgotPassword> findByAccount(Account account);
}
