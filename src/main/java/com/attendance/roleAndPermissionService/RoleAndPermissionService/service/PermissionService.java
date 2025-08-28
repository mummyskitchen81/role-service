package com.attendance.roleAndPermissionService.RoleAndPermissionService.service;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.repo.PermissionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepo permissionRepo;

    public ResponseEntity<?> getAllPermission() {

        List<String> permissionList=permissionRepo.getAllPermission();

        return ResponseEntity.ok(permissionList);
    }
}
