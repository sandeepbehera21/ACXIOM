package com.acxiom.crm.security;

import com.acxiom.crm.entity.AppUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adapts an {@link AppUser} to Spring Security.
 * <p>
 * This class is the Java equivalent of the specification's IdentityUser: the
 * application owns the user table because Java has no ASP.NET Core Identity.
 * See docs/STACK-MAPPING.md for the recorded mapping.
 */
public class AppUserPrincipal implements UserDetails {

    private final AppUser user;

    public AppUserPrincipal(AppUser user) {
        this.user = user;
    }

    public AppUser getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }

    /** Role names are exposed as ROLE_ authorities, e.g. ROLE_ADMIN. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Lockout is enforced here so that a locked account still fails
     * authentication even if the password is correct (p.8 §6.3).
     */
    @Override
    public boolean isAccountNonLocked() {
        return !user.isLockedOut();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
