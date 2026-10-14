package com.acxiom.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Forces the CSRF token to be resolved on every request so that Spring Security
 * writes the {@code XSRF-TOKEN} cookie.
 * <p>
 * Without this, Spring Security 6's deferred token loading means the cookie is
 * never sent to the browser until a state-changing request is made - which the
 * browser would then reject for having no token. The React client reads this
 * cookie and returns it in the {@code X-XSRF-TOKEN} header.
 * <p>
 * This is the replacement for ASP.NET's ValidateAntiForgeryToken: the mechanism
 * differs, the protection is the same, and it is required by p.4 §4.2 and
 * p.13 §14 ("Anti-forgery protection enabled for state-changing MVC forms").
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Resolving the token value is what triggers the cookie to be written.
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
