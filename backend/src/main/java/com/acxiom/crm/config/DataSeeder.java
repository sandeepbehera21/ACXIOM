package com.acxiom.crm.config;

import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.Role;
import com.acxiom.crm.entity.RoleName;
import com.acxiom.crm.repository.AppUserRepository;
import com.acxiom.crm.repository.RoleRepository;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the three roles and one starter account per role.
 * <p>
 * The specification requires exactly these three roles (p.9 §7) and requires a
 * working login for each so that role-based access difference can be
 * demonstrated (p.19 §17.19 steps 9-11). Seeding is idempotent: existing rows are
 * never overwritten, so a changed password survives a restart.
 * <p>
 * These are development credentials. They are printed once at startup and must
 * be changed or removed before any real deployment (p.13 §14).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, AppUserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<RoleName, Role> roles = new EnumMap<>(RoleName.class);
        for (RoleName name : RoleName.values()) {
            roles.put(name, roleRepository.findByRoleName(name)
                    .orElseGet(() -> roleRepository.save(new Role(name))));
        }

        seedUser("System Administrator", "admin@acxiomcrm.local", "Admin@123", roles.get(RoleName.ADMIN));
        seedUser("Sales Manager", "manager@acxiomcrm.local", "Manager@123", roles.get(RoleName.MANAGER));
        seedUser("Sales Executive", "sales@acxiomcrm.local", "Sales@123", roles.get(RoleName.SALES_EXECUTIVE));
    }

    private void seedUser(String name, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        AppUser user = new AppUser();
        user.setName(name);
        user.setEmail(email);
        // Hashed with BCrypt. The plain-text value never leaves this method and is
        // never logged (p.8 §6.2).
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
        log.info("Seeded {} account: {}", role.getRoleName().name(), email);
    }
}
