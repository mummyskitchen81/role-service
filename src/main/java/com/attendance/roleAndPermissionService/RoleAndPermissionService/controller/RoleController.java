package com.attendance.roleAndPermissionService.RoleAndPermissionService.controller;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.*;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<String>> createRole(@RequestBody RoleCreateRequestDto requestDto){
        return roleService.createRoleWithPermission(requestDto);
    }

    @GetMapping("/get-all-role")
    public ResponseEntity<ApiResponseDto<?>> getAllRole(){
        return roleService.getAllRole();
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDto<String>> deleteRole(@RequestBody DeleteRoleRequestDto requestDto){
        return roleService.deleteRole(requestDto);
    }

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

    @PostMapping("/add-permission-to-role")
    public ResponseEntity<ApiResponseDto<List<String>>> addPermissionToRole(@RequestBody AddOrDeletePermissionRequestDto requestDto){
        return roleService.addPermissionToRole(requestDto);
    }

    @PostMapping("/delete-permission-to-role")
    public ResponseEntity<ApiResponseDto<String>> deletePermissionToRole(@RequestBody AddOrDeletePermissionRequestDto requestDto){
        return roleService.deletePermissionToRole(requestDto);
    }

    @GetMapping("/health-check")
    public ResponseEntity<ApiResponseDto<String>> healthCheck(){
        return roleService.healthCheck();
    }
}
