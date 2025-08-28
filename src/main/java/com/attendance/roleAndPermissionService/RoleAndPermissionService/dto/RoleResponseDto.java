package com.attendance.roleAndPermissionService.RoleAndPermissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleResponseDto {

    private Long id;

    private String role;

    private String description;
}
