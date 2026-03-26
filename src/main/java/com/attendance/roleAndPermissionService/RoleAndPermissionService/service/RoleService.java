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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long EXPIRATION = 8; // minutes

    public RoleService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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

        // Step 1: Fetch role
        Role role = roleRepo.findByRole(deleteRequest.getRole())
                .orElseThrow(() -> new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        String roleName = role.getRole();

        // Step 2: Prevent default role deletion
        if (role.isDefaultRole()) {
            return ResponseEntity
                    .status(403)
                    .body(new ApiResponseDto<>(
                            false,
                            MessageEnum.ROLE_DELETE_NOT_ALLOWED.format(roleName),
                            null,
                            LocalDateTime.now()
                    ));
        }

        // Step 3: Delete role-permission mapping
        rolePermissionRepo.deleteAllByRole(role);

        // Step 4: Call Auth Service (remove role from users)
        authClient.deleteAllUserByRole(new RoleRequestDto(roleName));

        // Step 5: Delete role
        roleRepo.delete(role);

        // ✅ Step 6: Redis Cache Invalidation
        String basePattern = "role:*:" + roleName;

        Set<String> keys = redisTemplate.keys(basePattern);

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // Optional: direct key (if specific used)
        redisTemplate.delete("role:permissions:" + roleName);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        MessageEnum.ROLE_DELETE.format(roleName),
                        null,
                        LocalDateTime.now()
                )
        );
    }

    public ResponseEntity<ApiResponseDto<String>> updateRole(UpdateRoleRequestDto requestDto) {

        // Step 1: Fetch role
        Role role = roleRepo.findByRole(requestDto.getOldRole())
                .orElseThrow(() -> new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        String oldRoleName = role.getRole();
        String newRoleName = requestDto.getNewRole();

        // Step 2: Check duplication
        if (!oldRoleName.equals(newRoleName)) {

            boolean exists = roleRepo.existsByRole(newRoleName);
            if (exists) {
                throw new DuplicateResourceException(E_Code.SERVICE_DUPLICATE.getMessage());
            }

            role.setRole(newRoleName);
        }

        // Step 3: Update description
        if (requestDto.getNewDescription() != null && !requestDto.getNewDescription().isBlank()) {
            role.setDescription(requestDto.getNewDescription());
        }

        // ✅ Step 4: SAVE DB FIRST
        roleRepo.save(role);

        // ✅ Step 5: DELETE OLD CACHE (MOST IMPORTANT)
        redisTemplate.delete("role:permissions:" + oldRoleName);

        // ✅ Optional: delete new key (safety)
        redisTemplate.delete("role:permissions:" + newRoleName);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        MessageEnum.ROLE_UPDATED.format(oldRoleName, newRoleName),
                        null,
                        LocalDateTime.now()
                )
        );
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

        String roleName = role.getRole();

        List<String> permissionList=requestDto.getPermission();

        for(String permissionStr: permissionList) {
            Permission permission = permissionRepo.findByPermission(permissionStr).orElseThrow(() -> new PermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

            RolePermission rolePermission = RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .isDefault(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            rolePermissionRepo.save(rolePermission);
        }

        // ✅ Redis Invalidation
        String key = "role:permissions:" + roleName;
        redisTemplate.delete(key);


        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.PERMISSION_ADD_TO_ROLE.format(requestDto.getRole()),permissionList,LocalDateTime.now() ));
    }

    @Transactional
    public ResponseEntity<ApiResponseDto<List<String>>> deletePermissionToRole(AddOrDeletePermissionRequestDto deleteRequest) {

        // FETCH ROLE
        Role role = roleRepo.findByRole(deleteRequest.getRole())
                .orElseThrow(() -> new RoleNotFoundException(E_Code.SERVICE_404.getMessage()));

        String roleName = role.getRole();

        List<String> permissionList = deleteRequest.getPermission();
        List<RolePermission> permissionsToDelete = new ArrayList<>();

        // FIRST: Check if any requested permission is default
        for (String permissionStr : permissionList) {

            Permission permission = permissionRepo.findByPermission(permissionStr)
                    .orElseThrow(() -> new PermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

            RolePermission rolePermission = rolePermissionRepo.findByRoleAndPermission(role, permission)
                    .orElseThrow(() -> new RolePermissionNotFoundException(E_Code.SERVICE_404.getMessage()));

            if (rolePermission.getIsDefault()) {
                // Abort entire operation if any permission is default
                return ResponseEntity.ok(new ApiResponseDto<>(
                        false,
                        "OPERATION ABORTED: DEFAULT PERMISSIONS CANNOT BE REMOVED",
                        null,
                        LocalDateTime.now()
                ));
            }

            permissionsToDelete.add(rolePermission);
        }

        // SECOND: Delete all non-default permissions
        if (!permissionsToDelete.isEmpty()) {
            rolePermissionRepo.deleteAll(permissionsToDelete);
        }

        // ✅ Redis Invalidation
        String key = "role:permissions:" + roleName;
        redisTemplate.delete(key);

        return ResponseEntity.ok(new ApiResponseDto<>(
                true,
                MessageEnum.PERMISSION_REMOVE_FROM_ROLE_SUCCESS.format(
                        permissionList,
                        deleteRequest.getRole()
                ),
                permissionList,
                LocalDateTime.now()
        ));
    }

    public ResponseEntity<ApiResponseDto<String>> healthCheck() {
        return ResponseEntity.ok(new ApiResponseDto<>(true,MessageEnum.HEALTHY.getMeessage(), MessageEnum.HEALTHY.getMeessage(), LocalDateTime.now()));
    }

    public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getRolesByIds(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(
                            false,
                            "Role IDs list cannot be null or empty",
                            null,
                            LocalDateTime.now()
                    )
            );
        }

        List<Long> uniqueIds = ids.stream().distinct().toList();

        List<RoleResponseDto> finalRoles = new ArrayList<>();
        List<Long> idsToFetchFromDB = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        // ✅ REDIS LOOKUP
        for (Long id : uniqueIds) {

            String key = "role:id:" + id;

            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {

                RoleResponseDto dto = objectMapper.convertValue(cached, RoleResponseDto.class);
                finalRoles.add(dto);

            } else {
                idsToFetchFromDB.add(id);
            }
        }

        // ✅ DB FETCH
        if (!idsToFetchFromDB.isEmpty()) {

            List<Role> roles = roleRepo.findAllById(idsToFetchFromDB);

            Set<Long> foundIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = idsToFetchFromDB.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            if (!missingIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponseDto<>(
                                false,
                                "Roles not found for IDs: " + missingIds,
                                null,
                                LocalDateTime.now()
                        )
                );
            }

            for (Role role : roles) {

                RoleResponseDto dto = RoleResponseDto.builder()
                        .id(role.getId())
                        .role(role.getRole())
                        .description(role.getDescription())
                        .build();

                String key = "role:id:" + role.getId();

                redisTemplate.opsForValue().set(key, dto, Duration.ofHours(5));

                finalRoles.add(dto);
            }
        }

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        "Roles fetched successfully",
                        finalRoles,
                        LocalDateTime.now()
                )
        );
    }

    public ApiResponseDto<List<PermissionDto>> getPermissionsForRoles(List<String> roleNames) {

        // Remove duplicates only
        List<String> rolesInput = roleNames.stream()
                .distinct()
                .toList();

        // ✅ Validate roles
        List<Role> roles = roleRepo.findByRoleIn(rolesInput);

        if (roles.isEmpty() || roles.size() != rolesInput.size()) {

            List<String> foundRoles = roles.stream()
                    .map(Role::getRole)
                    .toList();

            List<String> missingRoles = rolesInput.stream()
                    .filter(r -> !foundRoles.contains(r))
                    .toList();

            throw new RoleNotFoundException("Invalid roles: " + missingRoles);
        }

        List<PermissionDto> finalPermissions = new ArrayList<>();
        List<String> rolesToFetchFromDB = new ArrayList<>();

        // Redis check
        for (String role : rolesInput) {

            String key = "role:permissions:" + role;

            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                finalPermissions.addAll((List<PermissionDto>) cached);
            } else {
                rolesToFetchFromDB.add(role);
            }
        }

        // DB fetch
        if (!rolesToFetchFromDB.isEmpty()) {

            List<Role> dbRoles = roles.stream()
                    .filter(r -> rolesToFetchFromDB.contains(r.getRole()))
                    .toList();

            List<RolePermission> mappings = rolePermissionRepo.findByRoleIn(dbRoles);

            Map<String, List<PermissionDto>> rolePermissionMap = mappings.stream()
                    .collect(Collectors.groupingBy(
                            rp -> rp.getRole().getRole(),
                            Collectors.mapping(
                                    rp -> PermissionDto.builder()
                                            .permission(rp.getPermission().getPermission())
                                            .description(rp.getPermission().getDescription())
                                            .build(),
                                    Collectors.toList()
                            )
                    ));

            for (String role : rolesToFetchFromDB) {

                List<PermissionDto> perms = rolePermissionMap.getOrDefault(role, new ArrayList<>());

                String key = "role:permissions:" + role;

                redisTemplate.opsForValue().set(key, perms, Duration.ofHours(5));

                finalPermissions.addAll(perms);
            }
        }

        // Remove duplicates
        List<PermissionDto> uniquePermissions = finalPermissions.stream()
                .distinct()
                .toList();

        return new ApiResponseDto<>(
                true,
                "Permissions fetched successfully",
                uniquePermissions,
                LocalDateTime.now()
        );
    }
    public ApiResponseDto<List<RoleResponseDto>> getRolesByNames(List<String> roleNames) {

        if (roleNames == null || roleNames.isEmpty()) {
            return new ApiResponseDto<>(
                    false,
                    "Role names list cannot be empty",
                    null,
                    LocalDateTime.now()
            );
        }

        List<String> normalizedRoles = roleNames.stream()
                .map(String::toUpperCase)
                .distinct()
                .toList();

        List<Role> roles = roleRepo.findByRoleIn(normalizedRoles);

        Set<String> foundRoles = roles.stream()
                .map(Role::getRole)
                .collect(Collectors.toSet());

        List<String> missingRoles = normalizedRoles.stream()
                .filter(r -> !foundRoles.contains(r))
                .toList();

        if (!missingRoles.isEmpty()) {
            return new ApiResponseDto<>(
                    false,
                    "Roles not found: " + missingRoles,
                    null,
                    LocalDateTime.now()
            );
        }

        List<RoleResponseDto> response = roles.stream()
                .map(role -> RoleResponseDto.builder()
                        .id(role.getId())
                        .role(role.getRole())
                        .description(role.getDescription())
                        .build())
                .toList();

        return new ApiResponseDto<>(
                true,
                "Roles fetched successfully",
                response,
                LocalDateTime.now()
        );
    }
}
