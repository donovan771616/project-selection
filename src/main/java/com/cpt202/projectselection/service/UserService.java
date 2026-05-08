package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.mapper.SysRoleMapper;
import com.cpt202.projectselection.mapper.SysUserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

    public String findEmailByUsername(String username) {
        SysUser user = userMapper.selectByUserName(username);
        return user != null ? user.getEmail() : null;
    }

    @Transactional
    public ActivationNotice register(RegisterRequest request) {
        if (userMapper.countByUserName(request.getUserName()) > 0) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.countByEmail(request.getEmail()) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        ActivationNotice notice = createActivationNotice(request.getEmail());
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setStudentNo(request.getStudentNo());
        user.setEmployeeNo(request.getEmployeeNo());
        user.setTitle(request.getTitle());
        user.setDeptName(request.getDeptName());
        user.setGrade(request.getGrade());
        user.setClassName(request.getClassName());
        user.setEmail(request.getEmail());
        user.setPhonenumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("0");
        user.setEmailActivated("0");
        user.setActivationToken(notice.getToken());
        user.setActivationExpiresAt(notice.getExpiresAt());
        user.setCreateBy("register");
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserId(), request.getRoleKey());
        return notice;
    }

    @Transactional
    public ActivationNotice registerStudent(StudentRegisterRequest request) {
        if (userMapper.countByUserName(request.getUserName()) > 0) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.countByEmail(request.getEmail()) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userMapper.countByStudentNo(request.getStudentNo()) > 0) {
            throw new IllegalArgumentException("Student ID already exists");
        }
        ActivationNotice notice = createActivationNotice(request.getEmail());
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setStudentNo(request.getStudentNo());
        user.setDeptName(request.getDeptName());
        user.setGrade(request.getGrade());
        user.setClassName(request.getClassName());
        user.setEmail(request.getEmail());
        user.setPhonenumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("0");
        user.setEmailActivated("0");
        user.setActivationToken(notice.getToken());
        user.setActivationExpiresAt(notice.getExpiresAt());
        user.setCreateBy("register");
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserId(), "student");
        return notice;
    }

    @Transactional
    public ActivationNotice registerTeacher(TeacherRegisterRequest request) {
        if (userMapper.countByUserName(request.getUserName()) > 0) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.countByEmail(request.getEmail()) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        ActivationNotice notice = createActivationNotice(request.getEmail());
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setEmployeeNo(request.getEmployeeNo());
        user.setTitle(request.getTitle());
        user.setDeptName(request.getDeptName());
        user.setEmail(request.getEmail());
        user.setPhonenumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("0");
        user.setEmailActivated("0");
        user.setActivationToken(notice.getToken());
        user.setActivationExpiresAt(notice.getExpiresAt());
        user.setCreateBy("register");
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserId(), "teacher");
        return notice;
    }

    @Transactional
    public void activateAccount(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Activation link is invalid");
        }
        SysUser user = userMapper.selectByActivationToken(token);
        if (user == null) {
            throw new IllegalArgumentException("Activation link is invalid");
        }
        if ("1".equals(user.getEmailActivated())) {
            return;
        }
        if (user.getActivationExpiresAt() == null || user.getActivationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Activation link has expired");
        }
        userMapper.activateUser(user.getUserId());
    }

    @Transactional
    public ActivationNotice resendActivation(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        SysUser user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No inactive account was found for this email");
        }
        if ("1".equals(user.getEmailActivated())) {
            throw new IllegalArgumentException("This account is already activated");
        }
        ActivationNotice notice = createActivationNotice(user.getEmail());
        userMapper.updateActivationToken(user.getUserId(), notice.getToken(), notice.getExpiresAt());
        return notice;
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
        user.setStudentNo(form.getStudentNo());
        user.setEmployeeNo(form.getEmployeeNo());
        user.setTitle(form.getTitle());
        user.setDeptName(form.getDeptName());
        user.setGrade(form.getGrade());
        user.setClassName(form.getClassName());
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
        user.setStudentNo(form.getStudentNo());
        user.setEmployeeNo(form.getEmployeeNo());
        user.setTitle(form.getTitle());
        user.setDeptName(form.getDeptName());
        user.setGrade(form.getGrade());
        user.setClassName(form.getClassName());
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
    public void updateBasicInfo(Long userId, ProfileForm form) {
        if (userMapper.countByEmailExcludeUser(form.getEmail(), userId) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        userMapper.updateBasicInfo(userId, form.getNickName(), form.getEmail(), form.getPhonenumber(), form.getSex());
    }

    @Transactional
    public void updateStudentInfo(Long userId, ProfileForm form) {
        userMapper.updateStudentInfo(userId, form.getStudentNo(), form.getGrade(), form.getClassName());
    }

    @Transactional
    public void updateTeacherInfo(Long userId, ProfileForm form) {
        userMapper.updateTeacherInfo(userId, form.getEmployeeNo(), form.getTitle(), form.getDeptName());
    }

    @Transactional
    public void updateSocietyInfo(Long userId, ProfileForm form) {
        userMapper.updateSocietyInfo(userId, form.getStudentNo(), form.getGrade(), form.getClassName());
    }

    @Transactional
    public void changePassword(Long userId, ProfileForm form) {
        SysUser user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
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
        userMapper.updatePassword(userId, passwordEncoder.encode(form.getNewPassword()));
    }

    @Transactional
    public void updateProfileWithDetails(Long userId, ProfileForm form, String operator) {
        if (userMapper.countByEmailExcludeUser(form.getEmail(), userId) > 0) {
            throw new IllegalArgumentException("Email already exists");
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setNickName(form.getNickName());
        user.setEmail(form.getEmail());
        user.setPhonenumber(form.getPhonenumber());
        user.setSex(form.getSex());
        user.setStudentNo(form.getStudentNo());
        user.setEmployeeNo(form.getEmployeeNo());
        user.setTitle(form.getTitle());
        user.setDeptName(form.getDeptName());
        user.setGrade(form.getGrade());
        user.setClassName(form.getClassName());
        user.setUpdateBy(operator);
        userMapper.updateProfileDetail(user);
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

    private ActivationNotice createActivationNotice(String email) {
        String token = UUID.randomUUID().toString().replace("-", "");
        return new ActivationNotice(email, token, LocalDateTime.now().plusHours(3));
    }
}
