package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.SysMenu;
import java.util.List;

public interface SysMenuMapper {

    List<SysMenu> selectMenusByUserId(Long userId);

    List<String> selectPermsByUserId(Long userId);
}
