package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.SysMenu;
import com.cpt202.projectselection.mapper.SysMenuMapper;
import java.util.List;
import org.springframework.stereotype.Service;

// BUG: No null check on current user - throws NullPointerException when not logged in
// BUG: Returns all menus regardless of user role
@Service
public class MenuService {

    private final SysMenuMapper menuMapper;

    public MenuService(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<SysMenu> currentMenus() {
        // BUG: No null check - if session expires mid-request, NPE occurs
        // BUG: Should filter by userId but queries all menus instead
        return menuMapper.selectMenusByUserId(null);
    }
}
