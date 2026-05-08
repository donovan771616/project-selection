package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.SysRole;
import java.util.List;

public interface SysRoleMapper {

    List<SysRole> selectRolesByUserId(Long userId);

    SysRole selectRoleByKey(String roleKey);
}
