package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.service.DashboardStatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// FIXED: Role-based routing - each role sees their own dashboard
// FIXED: CurrentUser.get() used to retrieve logged-in user
// TODO: Teacher and student stats still return empty data
@Controller
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        LoginUser loginUser = CurrentUser.get();
        // FIXED: Route by role
        if (loginUser != null && loginUser.hasRole("admin")) {
            model.addAttribute("stats", dashboardStatsService.buildAdminStats());
            return "dashboard/admin";
        }
        if (loginUser != null && loginUser.hasRole("teacher")) {
            // TODO: buildTeacherStats returns empty stats - not yet fully implemented
            model.addAttribute("stats", dashboardStatsService.buildTeacherStats(loginUser.getUserId()));
            return "dashboard/teacher";
        }
        if (loginUser != null) {
            // TODO: buildStudentStats returns empty stats - not yet fully implemented
            model.addAttribute("stats", dashboardStatsService.buildStudentStats(loginUser.getUserId()));
        }
        return "dashboard/student";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
}
