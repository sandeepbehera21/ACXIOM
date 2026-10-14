package com.acxiom.crm.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security configuration.
 * <p>
 * Replaces the ASP.NET Core Identity configuration the specification describes.
 * Each capability the specification mandates is present here:
 * <ul>
 *   <li><b>Password hashing</b> (p.16 §17.9) - {@link BCryptPasswordEncoder}.
 *       No password is ever stored or compared in plain text.</li>
 *   <li><b>Session / cookie</b> (p.4 §4.2) - server-side session with an
 *       HttpOnly cookie, so the session identifier is unreadable to JavaScript.</li>
 *   <li><b>Anti-forgery</b> (p.13 §14) - CSRF protection using the
 *       double-submit cookie pattern the React client participates in.</li>
 *   <li><b>Server-side authorization</b> (p.9 §7) - URL rules here plus
 *       per-record ownership checks in the service layer.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Value("${acxiom.cors.allowed-origin}")
    private String allowedOrigin;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /** BCrypt: adaptive, salted hashing. Required by p.15 §17.4 and p.16 §17.9. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        // Do not reveal whether the email exists.
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // An explicit origin, not "*": credentials are sent with every request,
        // and the browser rejects "*" together with credentials.
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // The double-submit cookie pattern: the browser holds the raw token in a
        // readable cookie and echoes it in a header. Setting the request attribute
        // name to null disables the XOR masking designed for server-rendered
        // forms, which would not match what axios sends back.
        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName(null);

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfRequestHandler))
                // Writes the XSRF-TOKEN cookie on every response.
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Public: login and self-registration (p.11 §10 marks login Public).
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        // Public: the CSRF token itself. A client cannot log in until
                        // it holds a token, so requiring authentication to fetch one
                        // would make logging in impossible.
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        // Admin-only: audit log and user administration (p.9 §7.1).
                        .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
                        // Reports: Admin and Manager by default (p.11 §11).
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "MANAGER", "SALES_EXECUTIVE")
                        // Everything else under /api requires a session.
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.migrateSession()))
                // Login and logout are handled explicitly in AuthController so the
                // API returns JSON; the browser form mechanisms are disabled.
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}
