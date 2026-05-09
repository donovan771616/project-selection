package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.mapper.SysRoleMapper;
import com.cpt202.projectselection.mapper.SysUserMapper;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserMapper userMapper, SysRoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<SysUser> listUsers(String keyword, String roleKey) {
        List<SysUser> users = userMapper.selectUsers(keyword, roleKey);
        users.forEach(user -> user.setRoles(roleMapper.selectRolesByUserId(user.getUserId())));
        return users;
    }

    public SysUser getUser(Long userId) {
        SysUser user = userMapper.selectByUserId(userId);
        if (user != null) {
            user.setRoles(roleMapper.selectRolesByUserId(userId));
        }
        return user;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userMapper.countByUserName(request.getUserName()) > 0) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.countByEmail(request.getEmail()) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreateBy("register");
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserId(), request.getRoleKey());
    }

    @Transactional
    public void createUser(AdminUserForm form, String operator) {
        if (userMapper.countByUserName(form.getUserName()) > 0) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.countByEmail(form.getEmail()) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        SysUser user = new SysUser();
        user.setUserName(form.getUserName());
        user.setNickName(form.getNickName());
        user.setEmail(form.getEmail());
        user.setPhonenumber(form.getPhonenumber());
        user.setSex(form.getSex());
        user.setStatus(form.getStatus());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setCreateBy(operator);
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserId(), form.getRoleKey());
    }

    @Transactional
    public void updateUser(AdminUserForm form, String operator) {
        if (userMapper.countByEmailExcludeUser(form.getEmail(), form.getUserId()) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        SysUser user = new SysUser();
        user.setUserId(form.getUserId());
        user.setNickName(form.getNickName());
        user.setEmail(form.getEmail());
        user.setPhonenumber(form.getPhonenumber());
        user.setSex(form.getSex());
        user.setStatus(form.getStatus());
        user.setUpdateBy(operator);
        userMapper.updateUser(user);
        userMapper.deleteUserRoles(form.getUserId());
        userMapper.insertUserRole(form.getUserId(), form.getRoleKey());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            userMapper.updatePassword(form.getUserId(), passwordEncoder.encode(form.getPassword()));
        }
    }

    @Transactional
    public void updateProfile(Long userId, ProfileForm form, String operator) {
        if (userMapper.countByEmailExcludeUser(form.getEmail(), userId) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setNickName(form.getNickName());
        user.setEmail(form.getEmail());
        user.setPhonenumber(form.getPhonenumber());
        user.setSex(form.getSex());
        user.setUpdateBy(operator);
        userMapper.updateProfile(user);
    }

    @Transactional
    public void changePassword(SysUser user, ProfileForm form) {
        if (form.getOldPassword() == null || form.getNewPassword() == null
                || form.getOldPassword().isBlank() || form.getNewPassword().isBlank()) {
            return;
        }
        if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        if (form.getNewPassword().length() < 8 || !form.getNewPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new IllegalArgumentException("New password must be at least 8 characters and contain letters and numbers");
        }
        userMapper.updatePassword(user.getUserId(), passwordEncoder.encode(form.getNewPassword()));
    }
}
