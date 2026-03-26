package com.attendance.roleAndPermissionService.RoleAndPermissionService.enums;

public enum DefaultRoleEnum {

    ADMIN("Full system access"),
    TEACHER("Teacher level restricted access"),
    STUDENT("Student level restricted access"),
    HOD("Head of Department access");

    private final String description;

    DefaultRoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}