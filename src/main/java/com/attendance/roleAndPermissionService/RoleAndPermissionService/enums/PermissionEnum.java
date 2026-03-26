package com.attendance.roleAndPermissionService.RoleAndPermissionService.enums;

public enum PermissionEnum {

    MANAGE_USER("CREATE,EDIT,DELETE,VIEW"),  //CREATE,EDIT,DELETE,VIEW

    MANAGE_TIMETABLE("CREATE,EDIT,DELETE"), //CREATE,EDIT,DELETE

    MANAGE_ROLE("CREATE,EDIT,DELETE"), // CREATE,EDIT,DELETE

    MANAGE_CURRICULAM("CREATE,EDIT,DELETE"), //CREATE,EDIT,DELETE

    MANAGE_ATTENDANCE("EDIT,DELETE"), //EDIT,DELETE

    MANAGE_MEDICAL_LEAVE("VIEW, APPROVE, REJECT"), //VIEW, APPROVE, REJECT,

    APPLY_MEDICAL_LEAVE("STUDENT Specific"),  //STUDENT

    REPORT_GENERATE("TEACHER Specific"),

    VIEW_ATTENDANCE("TEACHER Specific"),

    TAKE_ATTENDANCE("TEACHER Specific"),

    VIEW_TIMETABLE("EVERONE"),

    VIEW_ANALYTICS("ADMIN TEACHER HOD Specific");


    private final String meessage;

    PermissionEnum(String meessage){

        this.meessage = meessage;
    }

    public String getMeessage(){
        return meessage;
    }
}
