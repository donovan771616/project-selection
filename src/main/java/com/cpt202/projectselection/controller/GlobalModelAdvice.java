package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.service.MenuService;
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
    public void addSharedAttributes(Model model) {
        model.addAttribute("loginUser", CurrentUser.get());
        model.addAttribute("menus", menuService.currentMenus());
    }
}
