package com.attendance.roleAndPermissionService.RoleAndPermissionService.config;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.util.CustomAccessDeniedHandler;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.util.InternalSecretFilter;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.util.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Autowired
    private InternalSecretFilter internalSecretFilter;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex->
                        ex.accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(internalSecretFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, InternalSecretFilter.class);

        return http.build();
    }
}
