package com.cpt202.projectselection.config;

import com.cpt202.projectselection.security.CustomUserDetailsService;
import com.cpt202.projectselection.security.LoginFailureHandler;
import com.cpt202.projectselection.security.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          LoginSuccessHandler loginSuccessHandler,
                          @Lazy LoginFailureHandler loginFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
    }

    /**
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeRequests()
                    .antMatchers("/css/**", "/js/**", "/images/**", "/vendor/**", "/login",
                            "/register", "/register/student", "/register/teacher",
                            "/activate", "/activation-pending", "/activation-success",
                            "/activation/resend", "/activation-resend").permitAll()
                    .antMatchers("/system/users/**").hasRole("ADMIN")
                    .antMatchers("/project/categories/**", "/project/reports/**").hasRole("ADMIN")
                    .antMatchers("/project/applications/**").hasAnyRole("TEACHER", "ADMIN", "STUDENT")
                    .antMatchers("/project/topics/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                    .antMatchers("/teacher/applications/**").hasAnyRole("TEACHER", "ADMIN")
                    .anyRequest().authenticated()
                    .and()
                .csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/login/process")
                    .successHandler(loginSuccessHandler)
                    .failureHandler(loginFailureHandler)
                    .permitAll()
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
                    .and()
                .exceptionHandling()
                    .accessDeniedPage("/403");
        return http.build();
    }

    /**
     * @return
     */

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
