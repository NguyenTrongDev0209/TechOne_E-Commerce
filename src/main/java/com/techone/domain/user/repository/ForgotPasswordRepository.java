package com.techone.domain.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.ForgotPassword;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {
    Optional<ForgotPassword> findByOtpAndAccount(String otp, Account account);

    Optional<ForgotPassword> findByAccount(Account account);
}
