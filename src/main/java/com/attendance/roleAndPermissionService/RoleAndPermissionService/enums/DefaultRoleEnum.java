package com.attendance.roleAndPermissionService.RoleAndPermissionService.enums;

public enum DefaultRoleEnum {
    ADMIN("ADMIN");

    private final String meessage;

    DefaultRoleEnum(String meessage){

        this.meessage = meessage;
    }

    public String getMeessage(){
        return meessage;
    }
}
