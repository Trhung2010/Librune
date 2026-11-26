package com.librune.config;

import com.librune.service.JwtService;
import com.librune.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates /api/** requests using Bearer JWT tokens issued by /api/auth/login.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }
        return path == null || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(7).trim();
            if (!tokenBlacklistService.isBlacklisted(token)) {
                String username = jwtService.extractUsernameIfValid(token);
                if (username != null) {
                    try {
                        UserDetails details = userDetailsService.loadUserByUsername(username);
                        if (details.isEnabled()) {
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    details, null, details.getAuthorities());
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    } catch (Exception ignored) {
                        // Leave SecurityContext empty so SecurityFilterChain returns 401
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
