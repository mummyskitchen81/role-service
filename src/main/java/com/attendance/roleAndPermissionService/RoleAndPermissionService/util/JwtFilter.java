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
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ✅ Skip if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                handleException(response, "Invalid JWT Token");
                return;
            }

            String username = jwtService.extractUsername(token);

            // ✅ FIX: Extract roles as List
            List<String> roles = jwtService.extractRoles(token);

            // ✅ Fetch permissions for ALL roles
            List<PermissionDto> allPermissions = roles.stream()
                    .flatMap(role -> {
                        ResponseEntity<ApiResponseDto<List<PermissionDto>>> res =
                                rolePermissionService.getAllPermissionForRole(role);
                        return res.getBody().getData().stream();
                    })
                    .distinct()
                    .toList();

            // ✅ Convert to authorities
            List<GrantedAuthority> authorities = allPermissions.stream()
                    .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            e.printStackTrace();
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
                .timeStamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}