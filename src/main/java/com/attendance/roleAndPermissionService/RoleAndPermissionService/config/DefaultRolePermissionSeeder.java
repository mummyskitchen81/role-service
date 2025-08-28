package com.attendance.roleAndPermissionService.RoleAndPermissionService.config;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Permission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Role;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.RolePermission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.DefaultRoleEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.E_Code;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.RoleNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.PermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RolePermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Order(3)
@Component
public class DefaultRolePermissionSeeder implements CommandLineRunner {

    @Autowired
    private PermissionRepo permissionRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private RolePermissionRepo rolePermissionRepo;

    @Override
    public void run(String... args) throws Exception {

        Role roleAdmin = roleRepo.findByRole(DefaultRoleEnum.ADMIN.name())
                .orElseThrow(() -> new RoleNotFoundException(E_Code.CONFIG_404.getMessage()));

        // Get existing permissions already mapped to this role
        List<RolePermission> existingRolePermissions = rolePermissionRepo.findByRole(roleAdmin);
        List<Long> existingPermissionIds = existingRolePermissions.stream()
                .map(rp -> rp.getPermission().getId())
                .toList();

        // Find all permissions
        List<Permission> allPermissions = permissionRepo.findAll();

        List<RolePermission> newMappings = new ArrayList<>();

        for (Permission permission : allPermissions) {
            if (!existingPermissionIds.contains(permission.getId())) {
                RolePermission rolePermission = RolePermission.builder()
                        .role(roleAdmin)
                        .permission(permission)
                        .createdAt(LocalDateTime.now())
                        .build();

                newMappings.add(rolePermission);
            }
        }

        if (!newMappings.isEmpty()) {
            rolePermissionRepo.saveAll(newMappings);
            System.out.println("New permissions added to ADMIN role");
        } else {
            System.out.println("ℹNo new permissions to add");
        }
    }
}
