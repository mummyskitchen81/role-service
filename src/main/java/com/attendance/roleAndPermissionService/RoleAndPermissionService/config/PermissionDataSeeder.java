package com.attendance.roleAndPermissionService.RoleAndPermissionService.config;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Permission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.MessageEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.PermissionEnum;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.PermissionRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
@Order(2)
@AllArgsConstructor
public class PermissionDataSeeder implements CommandLineRunner {

    @Autowired
    private PermissionRepo permissionRepo;

    @Override
    public void run(String... args) throws Exception {
        List<Permission> defaultPermission = List.of(

                // ================= MANAGEMENT =================

                new Permission(null, PermissionEnum.MANAGE_USER.name(), PermissionEnum.MANAGE_USER.getMeessage()),
                new Permission(null, PermissionEnum.MANAGE_ROLE.name(), PermissionEnum.MANAGE_ROLE.getMeessage()),

                // ================= ACADEMIC OPERATION =================

                new Permission(null, PermissionEnum.MANAGE_TIMETABLE.name(), PermissionEnum.MANAGE_TIMETABLE.getMeessage()),
                new Permission(null, PermissionEnum.MANAGE_CURRICULAM.name(), PermissionEnum.MANAGE_CURRICULAM.getMeessage()),

                // ================= ATTENDANCE =================

                new Permission(null, PermissionEnum.MANAGE_ATTENDANCE.name(), PermissionEnum.MANAGE_ATTENDANCE.getMeessage()),
                new Permission(null, PermissionEnum.TAKE_ATTENDANCE.name(), PermissionEnum.TAKE_ATTENDANCE.getMeessage()),
                new Permission(null, PermissionEnum.VIEW_ATTENDANCE.name(), PermissionEnum.VIEW_ATTENDANCE.getMeessage()),

                // ================= LEAVE =================

                new Permission(null, PermissionEnum.APPLY_MEDICAL_LEAVE.name(), PermissionEnum.APPLY_MEDICAL_LEAVE.getMeessage()),
                new Permission(null, PermissionEnum.MANAGE_MEDICAL_LEAVE.name(), PermissionEnum.MANAGE_MEDICAL_LEAVE.getMeessage()),

                // ================= TIMETABLE =================

                new Permission(null, PermissionEnum.VIEW_TIMETABLE.name(), PermissionEnum.VIEW_TIMETABLE.getMeessage()),

                // ================= REPORT =================

                new Permission(null, PermissionEnum.REPORT_GENERATE.name(), PermissionEnum.REPORT_GENERATE.getMeessage()),

                // ================= ANALYTICS =================

                new Permission(null, PermissionEnum.VIEW_ANALYTICS.name(), PermissionEnum.VIEW_ANALYTICS.getMeessage())
        );

        for(Permission permission: defaultPermission){
            if(!permissionRepo.existsByPermission(permission.getPermission())){
                permissionRepo.save(permission);
            }
        }

        System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$ PERMISSION SEEDING COMPLETE $$$$$$$$$$$$$$$$$$$\n");
    }
}
