package com.attendance.roleAndPermissionService.RoleAndPermissionService.controller;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.ApiResponseDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.AssignPermissionToRoleRequestDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.DeletePermissionFromRoleRequestDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.PermissionDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rolePermission")
public class RoleAndPermissionController {
    @Autowired
    private RolePermissionService rolePermissionService;

    @PostMapping("/assign")
    public ResponseEntity<ApiResponseDto<String>> assignPermissionToRole(@RequestBody AssignPermissionToRoleRequestDto assignRequest){
        return rolePermissionService.assignPermissionToRole(assignRequest);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDto<String>> deletePermissionFromRole(@RequestBody DeletePermissionFromRoleRequestDto deleteRequest){
        return rolePermissionService.deletePermissionFromRole(deleteRequest);
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @GetMapping("/{role}/permission")
    public ResponseEntity<ApiResponseDto<List<PermissionDto>>> getAllPermissionForRole(@PathVariable String role){
        return rolePermissionService.getAllPermissionForRole(role);
    }
}
