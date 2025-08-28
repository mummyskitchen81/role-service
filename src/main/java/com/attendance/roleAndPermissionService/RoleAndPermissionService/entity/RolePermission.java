package com.attendance.roleAndPermissionService.RoleAndPermissionService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "role_permission",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"role_id","permission_id"})
        }
)
public class RolePermission {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(
            name = "fk_role_permission_role",
            foreignKeyDefinition = "FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE"
            )
    )
    private Role role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_id",nullable = false)
    private Permission permission;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
