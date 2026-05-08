package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.service.MenuService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAdvice {

    private final MenuService menuService;

    public GlobalModelAdvice(MenuService menuService) {
        this.menuService = menuService;
    }

    @ModelAttribute
    public void addSharedAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("loginUser", CurrentUser.get());
        
        // 只有已认证的用户才加载菜单
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && 
            !"anonymousUser".equals(auth.getPrincipal())) {
            model.addAttribute("menus", menuService.currentMenus());
        }
        
        model.addAttribute("currentUri", request.getRequestURI());
    }
}
