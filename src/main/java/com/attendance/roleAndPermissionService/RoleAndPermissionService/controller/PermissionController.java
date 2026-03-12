package com.attendance.roleAndPermissionService.RoleAndPermissionService.controller;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/getAllPermission")
    public ResponseEntity<?> getAllPermission(){
        return permissionService.getAllPermission();
    }

}
