package com.cpt202.projectselection.security;

import com.cpt202.projectselection.domain.SysRole;
import com.cpt202.projectselection.domain.SysUser;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class LoginUser implements UserDetails {

    private final SysUser user;
    private final Set<String> permissions;
    private final List<GrantedAuthority> authorities;

    public LoginUser(SysUser user, Set<String> permissions, List<GrantedAuthority> authorities) {
        this.user = user;
        this.permissions = permissions;
        this.authorities = authorities;
    }

    public SysUser getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public String getNickName() {
        return user.getNickName();
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public boolean hasRole(String roleKey) {
        return user.getRoles().stream().map(SysRole::getRoleKey).anyMatch(roleKey::equals);
    }

    public String primaryRole() {
        return user.getRoles().stream().map(SysRole::getRoleKey).collect(Collectors.joining(","));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"1".equals(user.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "0".equals(user.getStatus()) && "0".equals(user.getDelFlag());
    }
}
