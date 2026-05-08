package com.cpt202.projectselection.security;

import com.cpt202.projectselection.domain.SysRole;
import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.mapper.SysMenuMapper;
import com.cpt202.projectselection.mapper.SysRoleMapper;
import com.cpt202.projectselection.mapper.SysUserMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    public CustomUserDetailsService(SysUserMapper userMapper, SysRoleMapper roleMapper, SysMenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username does not exist");
        }
        
        // 检查邮箱激活状态，未激活则抛出 DisabledException 阻止登录
        if ("0".equals(user.getEmailActivated())) {
            throw new DisabledException("Email not activated");
        }
        
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getUserId());
        user.setRoles(roles);
        Set<String> permissions = new HashSet<>(menuMapper.selectPermsByUserId(user.getUserId()));
        boolean admin = roles.stream().anyMatch(role -> "admin".equals(role.getRoleKey()));
        if (admin) {
            permissions.add("*:*:*");
        }
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleKey().toUpperCase()))
                .collect(Collectors.toList());
        permissions.stream()
                .filter(permission -> permission != null && !permission.isBlank())
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return new LoginUser(user, permissions, authorities);
    }
}
