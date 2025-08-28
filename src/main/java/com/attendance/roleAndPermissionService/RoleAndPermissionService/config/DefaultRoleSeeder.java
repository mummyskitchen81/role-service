package com.attendance.roleAndPermissionService.RoleAndPermissionService.config;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Role;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.DefaultRoleEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.MessageEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Order(1)
@Component
public class DefaultRoleSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepo roleRepo;

    @Override
    public void run(String... args) throws Exception {
        if(!roleRepo.existsByRole(DefaultRoleEnum.ADMIN.name())){

            Role role=Role.builder()
                            .role(DefaultRoleEnum.ADMIN.name())
                                    .description(MessageEnum.ADMIN_FULL_PERMISSION.getMeessage())
                                            .defaultRole(true)
                                                    .createdAt(LocalDateTime.now())
                                                            .build();
            roleRepo.save(role);
        }
    }
}
