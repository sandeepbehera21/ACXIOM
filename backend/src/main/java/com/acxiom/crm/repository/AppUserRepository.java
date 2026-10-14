package com.acxiom.crm.repository;

import com.acxiom.crm.entity.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /** Login accepts either username or email; email is the identifier used here. */
    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findAllByOrderByNameAsc();

    List<AppUser> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(String name, String email);

    long countByActiveTrue();
}
