package com.attendance.roleAndPermissionService.RoleAndPermissionService.util;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.ApiResponseDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.PermissionDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.service.JWTService;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.service.RolePermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RolePermissionService rolePermissionService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // This stops

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException, IOException {

        // GUARD: If InternalSecretFilter already authenticated this (e.g. a GET request), skip JWT logic
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                "GET".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        System.out.println("@@@@@@@@@Hora ");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("I am hre############");

        String token = header.substring(7);

        try {
            // Any JWT parsing (isTokenValid, extractUsername) that fails will jump to catch
            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRoles(token);

            System.out.println("#####     Role: " + role + "     #########");

            // load permissions using roles
            ResponseEntity<ApiResponseDto<List<PermissionDto>>> permissions = rolePermissionService.getAllPermissionForRole(role);

            List<GrantedAuthority> authorities =
                    permissions.getBody().getData()
                            .stream()
                            .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                            .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            handleException(response, "JWT Token has expired");
            return;
        } catch (Exception e) {
            handleException(response, "Invalid JWT Token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        ApiResponseDto<Object> apiResponse = ApiResponseDto.builder()
                .success(false)
                .message(message)
                .data(null)
                .timeStamp(LocalDateTime.now()) // Now this will be "2026-03-11T14:32:03"
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}
