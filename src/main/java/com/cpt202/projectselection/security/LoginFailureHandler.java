package com.cpt202.projectselection.security;

import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.mapper.SysUserMapper;
import com.cpt202.projectselection.service.ActivationMailService;
import com.cpt202.projectselection.service.ActivationNotice;
import com.cpt202.projectselection.service.UserService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserService userService;
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivationMailService activationMailService;

    public LoginFailureHandler(UserService userService, 
                               SysUserMapper userMapper,
                               PasswordEncoder passwordEncoder,
                               ActivationMailService activationMailService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.activationMailService = activationMailService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String redirectUrl;
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 添加日志输出
        System.out.println("[LoginFailureHandler] Authentication failed for user: " + username);
        System.out.println("[LoginFailureHandler] Exception type: " + exception.getClass().getName());
        System.out.println("[LoginFailureHandler] Exception message: " + exception.getMessage());
        
        // 检查根异常（DisabledException可能被包装成InternalAuthenticationServiceException）
        Throwable cause = exception.getCause();
        boolean isEmailNotActivated = "Email not activated".equals(exception.getMessage()) ||
                (cause != null && "Email not activated".equals(cause.getMessage()));

        if (exception instanceof DisabledException || isEmailNotActivated) {
            // 账号被禁用或未激活邮箱
            if (isEmailNotActivated) {
                // 邮箱未激活，跳转到专门的重发验证码页面
                SysUser user = userMapper.selectByUserName(username);
                String email = (user != null) ? user.getEmail() : "";
                redirectUrl = "/activation-resend?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            } else {
                // 账号被禁用
                redirectUrl = "/login?disabled";
            }
        } else if (exception instanceof LockedException) {
            // 账号被锁定
            redirectUrl = "/login?disabled";
        } else if (exception instanceof BadCredentialsException) {
            // 凭证错误（用户名或密码错误）
            SysUser user = userMapper.selectByUserName(username);
            if (user != null && password != null) {
                // 用户名存在，检查密码是否匹配
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    // 密码错误
                    System.out.println("[LoginFailureHandler] Password incorrect for user: " + username);
                    redirectUrl = "/login?error";
                } else {
                    // 其他错误
                    redirectUrl = "/login?error";
                }
            } else {
                // 用户名不存在
                System.out.println("[LoginFailureHandler] Username does not exist: " + username);
                redirectUrl = "/login?error";
            }
        } else {
            // 其他认证异常
            redirectUrl = "/login?error";
        }
        
        System.out.println("[LoginFailureHandler] Redirecting to: " + redirectUrl);
        response.sendRedirect(response.encodeRedirectURL(redirectUrl));
    }
}
