package com.taskflow.repository;

import com.taskflow.entity.OtpVerification;
import com.taskflow.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findFirstByUserIdAndOtpTypeAndVerifiedFalseOrderByCreatedAtDesc(
            Long userId, OtpType otpType);

    long countByUserIdAndOtpTypeAndCreatedAtAfter(Long userId, OtpType otpType, LocalDateTime after);
}
