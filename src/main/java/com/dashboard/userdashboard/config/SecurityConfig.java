package com.dashboard.userdashboard.config;

import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    // ── CHAIN 1: Web / Thymeleaf (session-based) ──────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain webFilterChain(HttpSecurity http)
            throws Exception {

        http
                .securityMatcher(
                        "/login", "/logout", "/register",
                        "/forgot-password", "/reset-password",
                        "/dashboard/**",
                        "/css/**", "/js/**", "/images/**",
                        "/h2-console/**"
                )
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Fully public
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/forgot-password",
                                "/reset-password",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // H2 console — admin only but permit here,
                        // protect via URL pattern in prod
                        .requestMatchers("/h2-console/**").permitAll()

                        // Admin dashboard pages
                        .requestMatchers("/dashboard/admin/**")
                        .hasAuthority(
                                Role.ROLE_ADMIN.getAuthority())

                        // Manager dashboard pages
                        // Admin can also access manager pages
                        .requestMatchers("/dashboard/manager/**")
                        .hasAnyAuthority(
                                Role.ROLE_MANAGER.getAuthority(),
                                Role.ROLE_ADMIN.getAuthority())

                        // Redirect and access-denied — any logged in user
                        .requestMatchers(
                                "/dashboard/redirect",
                                "/dashboard/access-denied"
                        ).authenticated()

                        // User dashboard — any logged in user
                        .requestMatchers("/dashboard/user/**")
                        .authenticated()

                        // Catch-all
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard/redirect", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED)
                )

                // Critical — wire UserDetailsService to this chain
                .userDetailsService(userDetailsService)

                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/dashboard/access-denied")
                );

        // Allow H2 console iframe
        http.headers(h ->
                h.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }

    // ── CHAIN 2: REST API (JWT stateless) ─────────────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http)
            throws Exception {

        http
                .securityMatcher("/api/**", "/swagger-ui/**",
                        "/swagger-ui.html", "/api-docs/**",
                        "/v3/api-docs/**")
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**")
                        .hasAuthority(
                                Role.ROLE_ADMIN.getAuthority())
                        .requestMatchers("/api/manager/**")
                        .hasAnyAuthority(
                                Role.ROLE_MANAGER.getAuthority(),
                                Role.ROLE_ADMIN.getAuthority())
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .userDetailsService(userDetailsService)

                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept",
                "Origin", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}