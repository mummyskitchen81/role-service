package com.attendance.roleAndPermissionService.RoleAndPermissionService.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value; // Correct Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    @Value("${internal.api.secrets}")
    private List<String> expectedSecrets; // Spring automatically splits comma-separated strings into a list

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestSecret = request.getHeader("X-INTERNAL-SECRET");

        // Check if the provided header exists in our list of valid secrets
        boolean isSecretValid = requestSecret != null && expectedSecrets.contains(requestSecret);

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            if (isSecretValid) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "INTERNAL_SYSTEM", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } else if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()) ||
                "DELETE".equalsIgnoreCase(request.getMethod()) || "PATCH".equalsIgnoreCase(request.getMethod())) {
            if (!isSecretValid) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Missing Internal Secret");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}