package com.cpt202.projectselection.service;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.SysMenu;
import com.cpt202.projectselection.mapper.SysMenuMapper;
import com.cpt202.projectselection.security.LoginUser;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class MenuService {

    private final SysMenuMapper menuMapper;

    public MenuService(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<SysMenu> currentMenus() {
        LoginUser loginUser = CurrentUser.get();
        if (loginUser == null) {
            return Collections.emptyList();
        }
        return menuMapper.selectMenusByUserId(loginUser.getUserId());
    }
}
