package com.attendance.roleAndPermissionService.RoleAndPermissionService.config;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Permission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Role;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.RolePermission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.DefaultRoleEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.PermissionEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.E_Code;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.exception.RoleNotFoundException;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.PermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RolePermissionRepo;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RoleRepo;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Order(3)
@AllArgsConstructor
public class DefaultRolePermissionSeeder implements CommandLineRunner {

    private final PermissionRepo permissionRepo;
    private final RoleRepo roleRepo;
    private final RolePermissionRepo rolePermissionRepo;

    @Override
    public void run(String... args) {

        List<Permission> allPermissions = permissionRepo.findAll();

        Map<String, Permission> permissionMap = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getPermission, p -> p));

        /* ================= ROLE PERMISSION MATRIX ================= */

        Map<DefaultRoleEnum, List<String>> rolePermissionMatrix = new EnumMap<>(DefaultRoleEnum.class);

        // ADMIN
        rolePermissionMatrix.put(DefaultRoleEnum.ADMIN, List.of(
                PermissionEnum.MANAGE_USER.name(),
                PermissionEnum.MANAGE_TIMETABLE.name(),
                PermissionEnum.MANAGE_ROLE.name(),
                PermissionEnum.MANAGE_CURRICULAM.name(),
                PermissionEnum.REPORT_GENERATE.name(),
                PermissionEnum.VIEW_ATTENDANCE.name(),
                PermissionEnum.VIEW_TIMETABLE.name(),
                PermissionEnum.VIEW_ANALYTICS.name()
        ));

        // TEACHER
        rolePermissionMatrix.put(DefaultRoleEnum.TEACHER, List.of(
                PermissionEnum.TAKE_ATTENDANCE.name(),
                PermissionEnum.VIEW_ATTENDANCE.name(),
                PermissionEnum.VIEW_TIMETABLE.name(),
                PermissionEnum.REPORT_GENERATE.name(),
                PermissionEnum.MANAGE_ATTENDANCE.name()
        ));

        // STUDENT
        rolePermissionMatrix.put(DefaultRoleEnum.STUDENT, List.of(
                PermissionEnum.VIEW_TIMETABLE.name(),
                PermissionEnum.APPLY_MEDICAL_LEAVE.name()
        ));

        // HOD
        rolePermissionMatrix.put(DefaultRoleEnum.HOD, List.of(
                PermissionEnum.VIEW_ATTENDANCE.name(),
                PermissionEnum.VIEW_ANALYTICS.name(),
                PermissionEnum.MANAGE_MEDICAL_LEAVE.name(),
                PermissionEnum.REPORT_GENERATE.name(),
                PermissionEnum.VIEW_TIMETABLE.name()
        ));

        /* ================= SEEDING ================= */

        for (Map.Entry<DefaultRoleEnum, List<String>> entry : rolePermissionMatrix.entrySet()) {

            DefaultRoleEnum roleEnum = entry.getKey();

            Role role = roleRepo.findByRole(roleEnum.name())
                    .orElseThrow(() -> new RoleNotFoundException(E_Code.CONFIG_404.getMessage()));

            Set<Long> existingPermissionIds = rolePermissionRepo.findByRole(role)
                    .stream()
                    .map(rp -> rp.getPermission().getId())
                    .collect(Collectors.toSet());

            List<RolePermission> newMappings = new ArrayList<>();

            for (String permissionName : entry.getValue()) {

                Permission permission = permissionMap.get(permissionName);

                if (permission != null && !existingPermissionIds.contains(permission.getId())) {

                    newMappings.add(
                            RolePermission.builder()
                                    .role(role)
                                    .permission(permission)
                                    .isDefault(true)
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );
                }
            }

            if (!newMappings.isEmpty()) {
                rolePermissionRepo.saveAll(newMappings);
                System.out.println("Permissions seeded for role: " + roleEnum.name());
            }
        }

        System.out.println("\n$$$$$$$$ ROLE-PERMISSION SEEDING COMPLETE $$$$$$$$$$$\n");
    }
}