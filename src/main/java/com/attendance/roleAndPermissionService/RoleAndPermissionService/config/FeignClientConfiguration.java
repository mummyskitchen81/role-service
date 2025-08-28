package com.attendance.roleAndPermissionService.RoleAndPermissionService.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Service-Auth", "your-secret-key");
        };
    }
}
