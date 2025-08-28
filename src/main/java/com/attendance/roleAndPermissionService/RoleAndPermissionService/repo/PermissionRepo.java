package com.attendance.roleAndPermissionService.RoleAndPermissionService.repo;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepo extends JpaRepository<Permission,Long> {
    boolean existsByPermission(String permission);

    @Query(value = "SELECT permission FROM permission", nativeQuery = true)
    List<String> getAllPermission();

    Optional<Permission> findByPermission(String permission);
}
