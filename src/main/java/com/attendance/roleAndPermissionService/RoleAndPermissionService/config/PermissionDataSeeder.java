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
        List<Permission> defaultPermission=List.of(

                new Permission(null,PermissionEnum.CREATE.getMeessage(),MessageEnum.MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.EDIT.getMeessage(),MessageEnum.MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.DELETE.getMeessage(),MessageEnum.MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.PROFILE.getMeessage(),MessageEnum.MANAGEMENT.getMeessage()),

                new Permission(null,PermissionEnum.CREATE_TIMETABLE.getMeessage(),MessageEnum.ACADEMIC_OPERATION.getMeessage()),
                new Permission(null,PermissionEnum.EDIT_TIMETABLE.getMeessage(),MessageEnum.ACADEMIC_OPERATION.getMeessage()),
                new Permission(null,PermissionEnum.DELETE_TIMETABLE.getMeessage(),MessageEnum.ACADEMIC_OPERATION.getMeessage()),

                new Permission(null,PermissionEnum.ASSIGN_FACULTY.getMeessage(),MessageEnum.DEPARTMENTAL_MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.REPORT_GENERATE.getMeessage(),MessageEnum.DEPARTMENTAL_MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.MEDICAL_LEAVE.getMeessage(),MessageEnum.DEPARTMENTAL_MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.VIEW_ATTENDANCE.getMeessage(),MessageEnum.DEPARTMENTAL_MANAGEMENT.getMeessage()),

                new Permission(null,PermissionEnum.CREATE_ROLE.getMeessage(),MessageEnum.MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.DELETE_ROLE.getMeessage(),MessageEnum.MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.MANAGE_ROLE.getMeessage(),MessageEnum.MANAGEMENT.getMeessage()),
                new Permission(null,PermissionEnum.REGISTER_HOD.getMeessage(),MessageEnum.MANAGEMENT.getMeessage())
        );

        for(Permission permission: defaultPermission){
            if(!permissionRepo.existsByPermission(permission.getPermission())){
                permissionRepo.save(permission);
            }
        }

        System.out.println("Permission seedin SUCCESSFU");
    }
}
