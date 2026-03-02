package com.example.ragaitest.config;

import com.example.ragaitest.security.Principal;
import com.example.ragaitest.service.JwtService;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        if (isPublicEndpoint(requestPath, contextPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            token = extractTokenFromCookie(request);
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserEntity user = jwtService.getUserFromToken(token);
                
                var authorities = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>();

                Principal principal = new Principal(
                        user.getId(),
                        user.getUsername(),
                        authorities
                );

                var authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("Authenticated user: {}", user.getUsername());
            } catch (Exception e) {
                log.debug("Failed to authenticate: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String contextPath) {
        String servletPath = path.startsWith(contextPath) ? path.substring(contextPath.length()) : path;
        return servletPath.startsWith("/auth/") ||
               servletPath.equals("/register") ||
               servletPath.equals("/health") ||
               !servletPath.startsWith("/");
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
