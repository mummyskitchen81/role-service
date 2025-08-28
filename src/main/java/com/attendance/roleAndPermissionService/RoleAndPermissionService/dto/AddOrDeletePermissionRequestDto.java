package com.attendance.roleAndPermissionService.RoleAndPermissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddOrDeletePermissionRequestDto {

    private String role;

    private List<String> permission;
}
