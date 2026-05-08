package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.mapper.SysRoleMapper;
import com.cpt202.projectselection.mapper.SysUserMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceActivationTest {

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
    void registerCreatesInactiveUserWithThreeHourActivationToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("newstudent");
        request.setNickName("New Student");
        request.setStudentNo("2023456789");
        request.setGrade("2023");
        request.setClassName("Software A");
        request.setEmail("newstudent@example.com");
        request.setPassword("abc12345");
        request.setRoleKey("student");
        when(passwordEncoder.encode("abc12345")).thenReturn("encoded");

        ActivationNotice notice = userService.register(request);

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insertUser(userCaptor.capture());
        SysUser inserted = userCaptor.getValue();
        assertThat(inserted.getEmailActivated()).isEqualTo("0");
        assertThat(inserted.getActivationToken()).isNotBlank();
        assertThat(inserted.getActivationExpiresAt()).isAfter(LocalDateTime.now().plusHours(2).plusMinutes(55));
        assertThat(inserted.getActivationExpiresAt()).isBefore(LocalDateTime.now().plusHours(3).plusMinutes(5));
        assertThat(inserted.getStudentNo()).isEqualTo("2023456789");
        assertThat(inserted.getGrade()).isEqualTo("2023");
        assertThat(inserted.getClassName()).isEqualTo("Software A");
        assertThat(notice.getEmail()).isEqualTo("newstudent@example.com");
        assertThat(notice.getToken()).isEqualTo(inserted.getActivationToken());
        verify(userMapper).insertUserRole(inserted.getUserId(), "student");
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
    void activateAccountClearsTokenWhenStillValid() {
        SysUser user = new SysUser();
        user.setUserId(12L);
        user.setEmailActivated("0");
        user.setActivationToken("valid-token");
        user.setActivationExpiresAt(LocalDateTime.now().plusMinutes(30));
        when(userMapper.selectByActivationToken("valid-token")).thenReturn(user);

        userService.activateAccount("valid-token");

        verify(userMapper).activateUser(12L);
    }

    @Test
    void resendActivationRotatesTokenForInactiveAccount() {
        SysUser user = new SysUser();
        user.setUserId(15L);
        user.setEmail("teacher@example.com");
        user.setEmailActivated("0");
        when(userMapper.selectByEmail("teacher@example.com")).thenReturn(user);

        ActivationNotice notice = userService.resendActivation("teacher@example.com");

        assertThat(notice.getEmail()).isEqualTo("teacher@example.com");
        assertThat(notice.getToken()).isNotBlank();
        verify(userMapper).updateActivationToken(eq(15L), eq(notice.getToken()), any(LocalDateTime.class));
    }
}
