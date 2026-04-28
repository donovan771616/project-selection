package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.SysUser;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysUserMapper {

    List<SysUser> selectUsers(@Param("keyword") String keyword, @Param("roleKey") String roleKey);

    SysUser selectByUserName(String userName);

    SysUser selectByUserId(Long userId);

    int countByUserName(String userName);

    int countByEmail(String email);

    int insertUser(SysUser user);

    int insertUserRole(@Param("userId") Long userId, @Param("roleKey") String roleKey);

    int deleteUserRoles(Long userId);

    int updateUser(SysUser user);

    int updateProfile(SysUser user);

    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    int countByEmailExcludeUser(@Param("email") String email, @Param("userId") Long userId);
}
