package com.attendance.roleAndPermissionService.RoleAndPermissionService.repo;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Permission;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.Role;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepo extends JpaRepository<RolePermission,Long> {
    boolean existsByRole(Role role);

    boolean existsByPermission(Permission permission);

    Optional<RolePermission> findByRoleAndPermission(Role role,Permission permission);

    List<RolePermission> findByRole(Role role);

    void deleteAllByRole(Role role);

    List<RolePermission> findByRoleIn(List<Role> roles);
}
