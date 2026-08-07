package com.seafish.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}