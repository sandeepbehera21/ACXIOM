package com.acxiom.crm.security;

import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads users for Spring Security. Replaces ASP.NET Core Identity's UserManager.
 * <p>
 * <b>Only two outcomes are correct here:</b> return the {@link UserDetails}, or
 * throw {@link UsernameNotFoundException}. Locked and deactivated accounts are
 * <i>not</i> reported from this method - that state travels on
 * {@link AppUserPrincipal} and is evaluated by Spring Security's
 * pre-authentication checks, which raise {@code LockedException} or
 * {@code DisabledException}. Throwing those here looks equivalent but is not:
 * {@code DaoAuthenticationProvider} wraps every exception other than
 * {@code UsernameNotFoundException} in an
 * {@code InternalAuthenticationServiceException}, which the API can only report as
 * a 500 - a server error where a 401 belongs, and with the lockout check
 * bypassed entirely.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public CustomUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                // Deliberately the same message for an unknown user and a wrong
                // password, and converted to a 401 by the provider's
                // hideUserNotFoundExceptions setting, so the API never discloses
                // whether an account exists.
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));

        // Returns even when the account is locked or inactive; the principal
        // reports that state and Spring Security's checks reject the login with
        // the correct exception.
        return new AppUserPrincipal(user);
    }
}
