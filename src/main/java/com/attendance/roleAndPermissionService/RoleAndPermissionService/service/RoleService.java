package com.attendance.roleAndPermissionService.RoleAndPermissionService.service;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.*;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Permission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Role;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.RolePermission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.E_Code;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.MessageEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.DuplicateResourceException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.PermissionNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.RoleNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.RolePermissionNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.network.AuthClient;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.PermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RolePermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RolePermissionRepo rolePermissionRepo;

    @Autowired
    private AuthClient authClient;

    @Autowired
    private PermissionRepo permissionRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Transactional
    public ResponseEntity<ApiResponseDto<String>> createRoleWithPermission(RoleCreateRequestDto requestDto) {

        Optional<Role> roleOptional=roleRepo.findByRole(requestDto.getRole());

        if(roleOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseDto<>(false, MessageEnum.ROLE_ALREADY_EXIST.getMeessage(), null, LocalDateTime.now()));
        }

        Role role=Role.builder()
                .role(requestDto.getRole().toUpperCase())
                .description(requestDto.getDescription().toUpperCase())
                .createdAt(LocalDateTime.now())
                .build();

        roleRepo.save(role);

        List<String> permissionList=requestDto.getPermission();

        for(String permissionStr: permissionList){

            Permission permission=permissionRepo.findByPermission(permissionStr).orElseThrow(()->new PermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

            RolePermission rolePermission=RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .createdAt(LocalDateTime.now())
                    .build();

            rolePermissionRepo.save(rolePermission);
        }

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.ROLE_CREATE_SUCCESSFUL.getMeessage(), null,LocalDateTime.now()));

    }

    public ResponseEntity<ApiResponseDto<?>> getAllRole() {

        List<Role> roleList=roleRepo.findAll();

        List<RoleListResponseDto> roleListResponse= roleList.stream()
                .map(role-> new RoleListResponseDto(role.getRole(),role.getDescription()))
                .collect(Collectors.toList());

        ApiResponseDto<List<RoleListResponseDto>> response= ApiResponseDto.<List<RoleListResponseDto>>builder()
                .success(true)
                .message(MessageEnum.ROLES_FETCHED_SUCCESSFUL.getMeessage())
                .data(roleListResponse)
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<ApiResponseDto<String>> deleteRole(DeleteRoleRequestDto deleteRequest) {

        //FETCH ROLE
        Role role=roleRepo.findByRole(deleteRequest.getRole()).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        rolePermissionRepo.deleteAllByRole(role);

        authClient.deleteAllUserByRole(new RoleRequestDto(deleteRequest.getRole()));

        roleRepo.delete(role);

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.ROLE_DELETE.format(deleteRequest.getRole()),null,LocalDateTime.now()));
    }

    public ResponseEntity<ApiResponseDto<String>> updateRole(UpdateRoleRequestDto requestDto) {

        //FETCH ROLE
        Role role=roleRepo.findByRole(requestDto.getOldRole()).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        // Step 2: Check for duplication if changing the role name
        if(!role.getRole().equals(requestDto.getNewRole())){
            boolean exists = roleRepo.existsByRole(requestDto.getNewRole());
            if (exists) {
                throw new DuplicateResourceException(E_Code.SERVICE_DUPLICATE.getMessage());
            }
            role.setRole(requestDto.getNewRole());


        }

        // Step 3: Update description if provided
        if (requestDto.getNewDescription() != null && !requestDto.getNewDescription().isBlank()) {
            role.setDescription(requestDto.getNewDescription());
        }

        roleRepo.save(role);

        return ResponseEntity.ok(new ApiResponseDto<>(true, MessageEnum.ROLE_UPDATED.format(requestDto.getOldRole(), requestDto.getNewRole()), null, LocalDateTime.now()));
    }

    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleByName(String requestRole) {
        //FETCH ROLE
        Role role=roleRepo.findByRole(requestRole).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        RoleResponseDto responseDto=RoleResponseDto.builder()
                .id(role.getId())
                .role(role.getRole())
                .description(role.getDescription())
                .build();

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.ROLE_FETCH.format(requestRole),responseDto,LocalDateTime.now() ));
    }


    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleById(Long id) {

        Role role=roleRepo.findById(id).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        RoleResponseDto responseDto=RoleResponseDto.builder()
                .id(role.getId())
                .role(role.getRole())
                .description(role.getDescription())
                .build();

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.ROLE_FETCH.format(role.getRole()),responseDto,LocalDateTime.now() ));
    }

    public ResponseEntity<ApiResponseDto<List<String>>> addPermissionToRole(AddOrDeletePermissionRequestDto requestDto) {

        Role role=roleRepo.findByRole(requestDto.getRole()).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        List<String> permissionList=requestDto.getPermission();

        for(String permissionStr: permissionList) {
            Permission permission = permissionRepo.findByPermission(permissionStr).orElseThrow(() -> new PermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

            RolePermission rolePermission = RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .createdAt(LocalDateTime.now())
                    .build();

            rolePermissionRepo.save(rolePermission);
        }

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.PERMISSION_ADD_TO_ROLE.format(requestDto.getRole()),permissionList,LocalDateTime.now() ));
    }

    public ResponseEntity<ApiResponseDto<String>> deletePermissionToRole(AddOrDeletePermissionRequestDto deleteRequest) {
        //FETCH ROLE
        Role role=roleRepo.findByRole(deleteRequest.getRole()).orElseThrow(()->new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        //FETCH PERMISSION
        List<String> permissionList=deleteRequest.getPermission();

        for(String permissionStr: permissionList) {
            Permission permission = permissionRepo.findByPermission(permissionStr).orElseThrow(() -> new PermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

            //FETCH ROLEPERMISSION
            RolePermission rolePermission=rolePermissionRepo.findByRoleAndPermission(role, permission).orElseThrow(()-> new RolePermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

            rolePermissionRepo.delete(rolePermission);
        }

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.PERMISSION_REMOVE_FROM_ROLE_SUCCESS.format(
                deleteRequest.getPermission(),
                deleteRequest.getRole()
        ), null,LocalDateTime.now() ));
    }
}
