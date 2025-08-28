package com.attendance.roleAndPermissionService.RoleAndPermissionService.network;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.ApiResponseDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.RoleRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name="auth-service", url = "${auth.service.url}")
public interface AuthClient {

    @PostMapping("/delete-all-user-by-role")
    ApiResponseDto<String> deleteAllUserByRole(RoleRequestDto requestDto);
}
