package com.acxiom.crm.repository;

import com.acxiom.crm.entity.Role;
import com.acxiom.crm.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(RoleName roleName);
}
