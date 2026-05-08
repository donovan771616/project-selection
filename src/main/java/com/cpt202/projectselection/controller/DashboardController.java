package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.service.DashboardStatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// BUG: No role-based routing - all users land on admin dashboard
// BUG: CurrentUser not used - loginUser always null
@Controller
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        // BUG: Always builds admin stats regardless of who is logged in
        model.addAttribute("stats", dashboardStatsService.buildAdminStats());
        // BUG: Always returns admin view - teacher and student never see their dashboard
        return "dashboard/admin";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
}
