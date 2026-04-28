package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.common.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping({"/", "/index"})
    public String index() {
        LoginUser loginUser = CurrentUser.get();
        if (loginUser != null && loginUser.hasRole("admin")) {
            return "dashboard/admin";
        }
        if (loginUser != null && loginUser.hasRole("teacher")) {
            return "dashboard/teacher";
        }
        return "dashboard/student";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
}
