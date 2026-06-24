package com.dashboard.userdashboard.config;

import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.service.CustomUserDetailsService;
import com.dashboard.userdashboard.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Get the Authorization header
        final String authHeader = request.getHeader("Authorization");


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //  Extract the token
        // "Bearer eyJhbGciOiJIUzI1NiJ9..." → "eyJhbGciOiJIUzI1NiJ9..."
        final String jwt = authHeader.substring(7);

        try {
            // Extract email from token
            final String userEmail = jwtUtil.extractEmail(jwt);

            // Only proceed if:
            // 1. We got a valid email from the token
            // 2. No authentication is already set for this request
            //    (avoids processing the same request twice)
            if (userEmail != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                //  Load user from database
                User user = (User) userDetailsService
                        .loadUserByUsername(userEmail);

                // Validate the token
                // Checks signature AND expiry
                if (jwtUtil.isTokenValid(jwt, user)) {

                    // ── STEP 6: Create authentication object ──────
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );

                    // Attach request metadata (IP address, session)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Set in SecurityContext
                    // From this point, Spring Security knows who
                    // is making this request. @AuthenticationPrincipal
                    // in controllers reads from here.
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is malformed, expired, or has invalid signature.
            // We don't reject here — we just don't authenticate.
            // SecurityConfig rejects the request if the endpoint
            // requires authentication.
            log.debug("JWT processing failed for {}: {}",
                    request.getRequestURI(), e.getMessage());
        }

        // Continue the filter chain
        // Always call this — passes the request to the next filter
        // or to the DispatcherServlet if no more filters remain.
        filterChain.doFilter(request, response);
    }
}