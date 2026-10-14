package com.acxiom.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Application user.
 * <p>
 * Fields are the union of both data-model listings in the specification:
 * p.10 §9 "UserId, Name, Email, PasswordHash, RoleId, IsActive,
 * FailedLoginCount, LockoutEnd, CreatedDate".
 * <p>
 * The {@code passwordHash} is written only by Spring Security's
 * {@code BCryptPasswordEncoder} and is never returned by any DTO, view or log
 * (p.15 §17.4, p.18 §17.14).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt hash. Never plain text, never exposed. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Consecutive failed authentication attempts (p.8 §6.3). */
    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;

    /** Non-null while the account is temporarily locked (p.8 §6.3). */
    @Column(name = "lockout_end")
    private LocalDateTime lockoutEnd;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    /** True while a lockout is in force. */
    public boolean isLockedOut() {
        return lockoutEnd != null && lockoutEnd.isAfter(LocalDateTime.now());
    }
}
