package com.attendance.roleAndPermissionService.RoleAndPermissionService.controller;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.*;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<String>> createRole(@RequestBody RoleCreateRequestDto requestDto){
        return roleService.createRoleWithPermission(requestDto);
    }

    @GetMapping("/get-all-role")
    public ResponseEntity<ApiResponseDto<?>> getAllRole(){
        return roleService.getAllRole();
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDto<String>> deleteRole(@RequestBody DeleteRoleRequestDto requestDto){
        return roleService.deleteRole(requestDto);
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @PutMapping("/update-role")
    public ResponseEntity<ApiResponseDto<String>> updateRole(@RequestBody UpdateRoleRequestDto requestDto){
        return roleService.updateRole(requestDto);
    }

    @GetMapping("/get-role-by-name")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleByName(@RequestParam String role){
        return roleService.getRoleByName(role);

    }

    @GetMapping("/get-role-by-id/{id}")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleById(@PathVariable Long id){
        return roleService.getRoleById(id);
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @PostMapping("/add-permission-to-role")
    public ResponseEntity<ApiResponseDto<List<String>>> addPermissionToRole(@RequestBody AddOrDeletePermissionRequestDto requestDto){
        return roleService.addPermissionToRole(requestDto);
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @PostMapping("/delete-permission-to-role")
    public ResponseEntity<ApiResponseDto<List<String>>> deletePermissionToRole(@RequestBody AddOrDeletePermissionRequestDto requestDto){
        return roleService.deletePermissionToRole(requestDto);
    }

    @GetMapping("/health-check")
    public ResponseEntity<ApiResponseDto<String>> healthCheck(){
        return roleService.healthCheck();
    }

    @GetMapping("/get-roles-by-ids")
    public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getRolesByIds(@RequestParam List<Long> ids){
        return roleService.getRolesByIds(ids);
    }

    @GetMapping("/permissions")
    public ApiResponseDto<List<PermissionDto>> getPermissionsForRoles(
            @RequestParam List<String> roles) {
        return roleService.getPermissionsForRoles(roles);
    }

    @GetMapping("/get-roles-by-names")
    public ApiResponseDto<List<RoleResponseDto>> getRolesByNames(@RequestParam List<String> roles) {
        return roleService.getRolesByNames(roles);
    }
}
