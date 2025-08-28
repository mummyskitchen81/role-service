package com.attendance.roleAndPermissionService.RoleAndPermissionService.exception;

import com.attendance.roleAndPermissionService.RoleAndPermissionService.dto.ApiResponseDto;
import com.attendance.roleAndPermissionService.RoleAndPermissionService.enums.MessageEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalException {

  @ExceptionHandler(RoleNotFoundException.class)
  public ResponseEntity<ApiResponseDto<String>> roleNotFound(RoleNotFoundException e){
    ApiResponseDto<String> responseDto=ApiResponseDto.<String>builder()
            .success(false)
            .message(MessageEnum.ROLE_NOT_FOUND.getMeessage())
            .data(e.getMessage())
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(PermissionNotFoundException.class)
  public ResponseEntity<ApiResponseDto<String>> roleNotFound(PermissionNotFoundException e){
    ApiResponseDto<String> responseDto=ApiResponseDto.<String>builder()
            .success(false)
            .message(MessageEnum.INVALID_PERMISSION.getMeessage())
            .data(e.getMessage())
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RolePermissionNotFoundException.class)
  public ResponseEntity<ApiResponseDto<String>> rolePermissionNotFoundException(RolePermissionNotFoundException ex){
    ApiResponseDto<String> responseDto=ApiResponseDto.<String>builder()
            .success(false)
            .message(MessageEnum.INVALID_ROLE_OR_PERMISSION.getMeessage())
            .data(ex.getMessage())
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(responseDto,  HttpStatus.NOT_FOUND);

  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponseDto<String>> duplicateResource(DuplicateResourceException ex){
    ApiResponseDto<String> responseDto=ApiResponseDto.<String>builder()
            .success(false)
            .message(MessageEnum.ROLE_ALREADY_EXIST.getMeessage())
            .data(ex.getMessage())
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(responseDto,HttpStatus.CONFLICT);
  }

}
