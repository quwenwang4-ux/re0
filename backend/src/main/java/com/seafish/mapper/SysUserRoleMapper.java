package com.seafish.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserRoleMapper {

    @Insert("""
            INSERT INTO sys_user_role (
                user_id,
                role_id
            )
            SELECT
                #{userId},
                id
            FROM sys_role
            WHERE role_code = #{roleCode}
              AND status = 'ACTIVE'
            """)
    int assignRole(
            @Param("userId") Long userId,
            @Param("roleCode") String roleCode
    );

    @Select("""
            SELECT r.role_code
            FROM sys_user_role ur
            JOIN sys_role r
              ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND ur.status = 'ACTIVE'
              AND r.status = 'ACTIVE'
            ORDER BY r.id
            """)
    List<String> selectActiveRoleCodes(
            @Param("userId") Long userId
    );

    @Insert("""
        INSERT INTO sys_user_role (
            user_id,
            role_id,
            assigned_by,
            status
        )
        SELECT
            #{userId},
            id,
            #{assignedBy},
            'ACTIVE'
        FROM sys_role
        WHERE role_code = #{roleCode}
          AND status = 'ACTIVE'
        ON DUPLICATE KEY UPDATE
            assigned_by = #{assignedBy},
            assigned_at = CURRENT_TIMESTAMP,
            status = 'ACTIVE',
            updated_at = CURRENT_TIMESTAMP
        """)
    int assignOrReactivateRole(
            @Param("userId") Long userId,
            @Param("roleCode") String roleCode,
            @Param("assignedBy") Long assignedBy
    );

    @Select("""
            SELECT r.role_code AS label, COUNT(DISTINCT ur.user_id) AS value
            FROM sys_user_role ur
            JOIN sys_role r ON r.id = ur.role_id
            JOIN sys_user u ON u.id = ur.user_id
            WHERE ur.status = 'ACTIVE'
              AND r.status = 'ACTIVE'
              AND u.status = 'ACTIVE'
              AND u.deleted = 0
            GROUP BY r.role_code
            ORDER BY r.role_code
            """)
    List<Map<String, Object>> countUsersByRole();
}
