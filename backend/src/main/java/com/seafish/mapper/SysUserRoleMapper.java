package com.seafish.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}
