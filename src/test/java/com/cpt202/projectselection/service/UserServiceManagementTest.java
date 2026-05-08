package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.mapper.SysRoleMapper;
import com.cpt202.projectselection.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceManagementTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper, roleMapper, passwordEncoder);
    }

    @Test
    void activateAccountRejectsNullToken() {
        assertThatThrownBy(() -> userService.activateAccount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Activation link is invalid");
    }

    @Test
    void activateAccountRejectsBlankToken() {
        assertThatThrownBy(() -> userService.activateAccount("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Activation link is invalid");
    }

    @Test
    void activateAccountRejectsUnknownToken() {
        when(userMapper.selectByActivationToken("unknown")).thenReturn(null);

        assertThatThrownBy(() -> userService.activateAccount("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Activation link is invalid");
    }

    @Test
    void activateAccountRejectsExpiredToken() {
        SysUser user = new SysUser();
        user.setUserId(12L);
        user.setEmailActivated("0");
        user.setActivationToken("expired-token");
        user.setActivationExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userMapper.selectByActivationToken("expired-token")).thenReturn(user);

        assertThatThrownBy(() -> userService.activateAccount("expired-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Activation link has expired");
    }

    @Test
    void activateAccountNoOpForAlreadyActivated() {
        SysUser user = new SysUser();
        user.setUserId(12L);
        user.setEmailActivated("1");
        user.setActivationToken("some-token");
        user.setActivationExpiresAt(LocalDateTime.now().plusMinutes(30));
        when(userMapper.selectByActivationToken("some-token")).thenReturn(user);

        userService.activateAccount("some-token");
    }

    @Test
    void resendActivationRejectsBlankEmail() {
        assertThatThrownBy(() -> userService.resendActivation("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    void resendActivationRejectsUnknownEmail() {
        when(userMapper.selectByEmail("unknown@example.com")).thenReturn(null);

        assertThatThrownBy(() -> userService.resendActivation("unknown@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No inactive account was found for this email");
    }

    @Test
    void resendActivationRejectsAlreadyActivated() {
        SysUser user = new SysUser();
        user.setUserId(12L);
        user.setEmail("active@example.com");
        user.setEmailActivated("1");
        when(userMapper.selectByEmail("active@example.com")).thenReturn(user);

        assertThatThrownBy(() -> userService.resendActivation("active@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This account is already activated");
    }

    @Test
    void resendActivationRotatesToken() {
        SysUser user = new SysUser();
        user.setUserId(12L);
        user.setEmail("inactive@example.com");
        user.setEmailActivated("0");
        when(userMapper.selectByEmail("inactive@example.com")).thenReturn(user);

        ActivationNotice notice = userService.resendActivation("inactive@example.com");

        assertThat(notice.getEmail()).isEqualTo("inactive@example.com");
        assertThat(notice.getToken()).isNotBlank();
        assertThat(notice.getExpiresAt()).isAfter(LocalDateTime.now().plusHours(2).plusMinutes(55));
        verify(userMapper).updateActivationToken(eq(12L), eq(notice.getToken()), any(LocalDateTime.class));
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        AdminUserForm form = createAdminForm();
        form.setUserName("existinguser");
        when(userMapper.countByUserName("existinguser")).thenReturn(1);

        assertThatThrownBy(() -> userService.createUser(form, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        AdminUserForm form = createAdminForm();
        when(userMapper.countByUserName("newuser")).thenReturn(0);
        when(userMapper.countByEmail("new@example.com")).thenReturn(1);

        assertThatThrownBy(() -> userService.createUser(form, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void createUserInsertsUserAndRole() {
        AdminUserForm form = createAdminForm();
        when(userMapper.countByUserName("newuser")).thenReturn(0);
        when(userMapper.countByEmail("new@example.com")).thenReturn(0);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");

        userService.createUser(form, "admin");

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insertUser(captor.capture());
        SysUser inserted = captor.getValue();
        assertThat(inserted.getUserName()).isEqualTo("newuser");
        assertThat(inserted.getNickName()).isEqualTo("New User");
        assertThat(inserted.getEmail()).isEqualTo("new@example.com");
        assertThat(inserted.getCreateBy()).isEqualTo("admin");
        verify(userMapper).insertUserRole(inserted.getUserId(), "student");
    }

    @Test
    void updateUserChangesEmailAndRoles() {
        AdminUserForm form = createAdminForm();
        form.setUserId(5L);
        form.setPassword(null);
        when(userMapper.countByEmailExcludeUser("new@example.com", 5L)).thenReturn(0);

        userService.updateUser(form, "admin");

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateUser(captor.capture());
        verify(userMapper).deleteUserRoles(5L);
        verify(userMapper).insertUserRole(5L, "student");
    }

    @Test
    void updateUserUpdatesPasswordWhenProvided() {
        AdminUserForm form = createAdminForm();
        form.setUserId(5L);
        form.setPassword("NewPassword123");
        when(userMapper.countByEmailExcludeUser("new@example.com", 5L)).thenReturn(0);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newEncoded");

        userService.updateUser(form, "admin");

        verify(userMapper).updatePassword(5L, "newEncoded");
    }

    @Test
    void changePasswordRejectsNullUser() {
        ProfileForm form = new ProfileForm();
        form.setOldPassword("OldPass123");
        form.setNewPassword("NewPass123");

        // Calling changePassword with null userId triggers NPE due to Long method being called first
        assertThatThrownBy(() -> userService.changePassword((Long) null, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        SysUser user = new SysUser();
        user.setUserId(5L);
        user.setPassword("encodedOld");
        ProfileForm form = new ProfileForm();
        form.setOldPassword("WrongPassword");
        form.setNewPassword("NewPass123");
        when(passwordEncoder.matches("WrongPassword", "encodedOld")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(user, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Old password is incorrect");
    }

    @Test
    void changePasswordRejectsWeakNewPassword() {
        SysUser user = new SysUser();
        user.setUserId(5L);
        user.setPassword("encodedOld");
        ProfileForm form = new ProfileForm();
        form.setOldPassword("OldPass123");
        form.setNewPassword("weak");
        when(passwordEncoder.matches("OldPass123", "encodedOld")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(user, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 characters");
    }

    @Test
    void changePasswordSucceedsWithValidInput() {
        SysUser user = new SysUser();
        user.setUserId(5L);
        user.setPassword("encodedOld");
        ProfileForm form = new ProfileForm();
        form.setOldPassword("OldPass123");
        form.setNewPassword("NewPass123");
        when(passwordEncoder.matches("OldPass123", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("newEncoded");

        userService.changePassword(user, form);

        verify(userMapper).updatePassword(5L, "newEncoded");
    }

    @Test
    void changePasswordIgnoresBlankPasswords() {
        SysUser user = new SysUser();
        user.setUserId(5L);
        user.setPassword("encodedOld");
        ProfileForm form = new ProfileForm();
        form.setOldPassword("");
        form.setNewPassword("");

        // Should silently return without updating password
        userService.changePassword(user, form);
    }

    private AdminUserForm createAdminForm() {
        AdminUserForm form = new AdminUserForm();
        form.setUserName("newuser");
        form.setNickName("New User");
        form.setEmail("new@example.com");
        form.setPassword("Password123");
        form.setRoleKey("student");
        return form;
    }
}
