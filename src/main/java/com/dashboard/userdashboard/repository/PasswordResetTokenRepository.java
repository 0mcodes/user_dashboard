package com.dashboard.userdashboard.repository;

import com.dashboard.userdashboard.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Look up a token by its UUID string
    // Used when the user clicks the reset link
    Optional<PasswordResetToken> findByToken(String token);

    // Find all unused tokens for an email
    // Used to invalidate old tokens when a new reset is requested
    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.used = false")
    List<PasswordResetToken> findActiveTokensByEmail(@Param("email") String email);

    // Scheduled cleanup — deletes expired and used tokens weekly
    // Keeps the table from growing forever
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :cutoff OR t.used = true")
    void deleteExpiredAndUsedTokens(@Param("cutoff") LocalDateTime cutoff);
}
