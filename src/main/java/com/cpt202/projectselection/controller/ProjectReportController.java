package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.service.DashboardStatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectReportController {

    private final DashboardStatsService dashboardStatsService;

    public ProjectReportController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/project/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String reports(Model model) {
        model.addAttribute("stats", dashboardStatsService.buildReportStats());
        return "project/reports";
    }
}
