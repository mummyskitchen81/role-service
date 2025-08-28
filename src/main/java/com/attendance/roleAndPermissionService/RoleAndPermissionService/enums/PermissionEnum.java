package com.attendance.roleAndPermissionService.RoleAndPermissionService.enums;

public enum PermissionEnum {

    CREATE("CREATE"),
    EDIT("EDIT"),
    DELETE("DELETE"),
    PROFILE("PROFILE"),

    CREATE_TIMETABLE("CREATE_TIMETABLE"),
    EDIT_TIMETABLE("EDIT_TIMETABLE"),
    DELETE_TIMETABLE("DELETE_TIMETABLE"),

    ASSIGN_FACULTY("ASSIGN_FACULTY"),
    REPORT_GENERATE("REPORT_GENERATE"),
    MEDICAL_LEAVE("MEDICAL_LEAVE"),
    VIEW_ATTENDANCE("VIEW_ATTENDANCE"),



    CREATE_ROLE("CREATE_ROLE"),
    DELETE_ROLE("DELETE_ROLE"),
    MANAGE_ROLE("MANAGE_ROLE"),

    REGISTER_HOD("REGISTER_HOD");




    private final String meessage;

    PermissionEnum(String meessage){

        this.meessage = meessage;
    }

    public String getMeessage(){
        return meessage;
    }
}
