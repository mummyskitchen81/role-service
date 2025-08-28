package com.attendance.roleAndPermissionService.RoleAndPermissionService.enums;

public enum E_Code {

    SERVICE_404("SERVICE_404"),
    CONFIG_404("CONFIG_404"),
    SERVICE_DUPLICATE("SQL ERROR: 1062");

    private  final String message;

     E_Code(String message){
        this.message=message;
    }

    public String getMessage() {
        return message;
    }
}
