package com.attendance.roleAndPermissionService.RoleAndPermissionService.service;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.ApiResponseDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.AssignPermissionToRoleRequestDto;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.DeletePermissionFromRoleRequestDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.PermissionDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Permission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Role;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.RolePermission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.E_Code;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.MessageEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.PermissionNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.RoleNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.RolePermissionNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.PermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RolePermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class RolePermissionService {

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PermissionRepo permissionRepo;

    @Autowired
    private RolePermissionRepo rolePermissionRepo;

    public ResponseEntity<ApiResponseDto<String>> assignPermissionToRole(AssignPermissionToRoleRequestDto assignRequest) {

        //FETCH THROUGH ROLE
        Role role=roleRepo.findByRole(assignRequest.getRole()).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        //FETCH PERMISSION
        Permission permission=permissionRepo.findByPermission(assignRequest.getPermission()).orElseThrow(()->new PermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

        RolePermission rolePermission=RolePermission.builder()
                .role(role)
                .permission(permission)
                .createdAt(LocalDateTime.now())
                .build();

        rolePermissionRepo.save(rolePermission);

        return ResponseEntity.ok(new ApiResponseDto<>(true, MessageEnum.PERMISSION_ASSIGN.getMeessage(), null, LocalDateTime.now()));
    }

    public ResponseEntity<ApiResponseDto<String>> deletePermissionFromRole(DeletePermissionFromRoleRequestDto deleteRequest) {

        //FETCH ROLE
        Role role=roleRepo.findByRole(deleteRequest.getRole()).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        //FETCH PERMISSION
        Permission permission=permissionRepo.findByPermission(deleteRequest.getPermission()).orElseThrow(()->new PermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

        //FETCH ROLEPERMISSION
        RolePermission rolePermission=rolePermissionRepo.findByRoleAndPermission(role, permission).orElseThrow(()-> new RolePermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

        rolePermissionRepo.delete(rolePermission);

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.PERMISSION_REMOVE_FROM_ROLE_SUCCESS.format(
                deleteRequest.getPermission(),
                deleteRequest.getRole()
        ), null,LocalDateTime.now() ));
    }

    public ResponseEntity<ApiResponseDto<List<PermissionDto>>> getAllPermissionForRole(String roleRequest) {
        //FETCH ROLE
        Role role=roleRepo.findByRole(roleRequest).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        //FETCH ROLEPERMISSION
        List<RolePermission> rolePermission=rolePermissionRepo.findByRole(role);

        // EXTRACT PERMISSIONS
        List<Permission> permissions = rolePermission.stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());

        List<PermissionDto> permission = permissions.stream()
                .map(p -> new PermissionDto(p.getPermission(),p.getDescription()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        MessageEnum.PERMISSION_FETCH_BY_ROLE.format(roleRequest),
                        permission,
                        LocalDateTime.now()
                )
        );
    }
}
