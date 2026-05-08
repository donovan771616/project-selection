package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.SysUser;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysUserMapper {

    List<SysUser> selectUsers(@Param("keyword") String keyword, @Param("roleKey") String roleKey);

    SysUser selectByUserName(String userName);

    SysUser selectByUserId(Long userId);

    SysUser selectByEmail(String email);

    SysUser selectByActivationToken(String activationToken);

    int countByUserName(String userName);

    int countByEmail(String email);

    int countByStudentNo(String studentNo);

    int insertUser(SysUser user);

    int insertUserRole(@Param("userId") Long userId, @Param("roleKey") String roleKey);

    int deleteUserRoles(Long userId);

    int updateUser(SysUser user);

    int updateProfile(SysUser user);

    int updateProfileDetail(SysUser user);

    int updateBasicInfo(@Param("userId") Long userId,
                        @Param("nickName") String nickName,
                        @Param("email") String email,
                        @Param("phonenumber") String phonenumber,
                        @Param("sex") String sex);

    int updateStudentInfo(@Param("userId") Long userId,
                          @Param("studentNo") String studentNo,
                          @Param("grade") String grade,
                          @Param("className") String className);

    int updateTeacherInfo(@Param("userId") Long userId,
                          @Param("employeeNo") String employeeNo,
                          @Param("title") String title,
                          @Param("deptName") String deptName);

    int updateSocietyInfo(@Param("userId") Long userId,
                          @Param("studentNo") String studentNo,
                          @Param("grade") String grade,
                          @Param("className") String className);

    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    int countByEmailExcludeUser(@Param("email") String email, @Param("userId") Long userId);

    int countByRoleKey(String roleKey);

    int activateUser(Long userId);

    int updateActivationToken(@Param("userId") Long userId,
                              @Param("activationToken") String activationToken,
                              @Param("activationExpiresAt") LocalDateTime activationExpiresAt);
}
