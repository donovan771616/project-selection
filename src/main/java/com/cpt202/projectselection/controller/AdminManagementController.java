package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.DashboardStatsService;
import com.cpt202.projectselection.service.ProjectApplicationService;
import com.cpt202.projectselection.service.ProjectCategoryService;
import com.cpt202.projectselection.service.ProjectTopicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminManagementController {

    private final ProjectTopicService topicService;
    private final ProjectApplicationService applicationService;
    private final ProjectCategoryService categoryService;
    private final DashboardStatsService dashboardStatsService;

    public AdminManagementController(ProjectTopicService topicService,
                                     ProjectApplicationService applicationService,
                                     ProjectCategoryService categoryService,
                                     DashboardStatsService dashboardStatsService) {
        this.topicService = topicService;
        this.applicationService = applicationService;
        this.categoryService = categoryService;
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/admin/topics")
    @PreAuthorize("hasRole('ADMIN')")
    public String listTopics(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) Integer page,
                             Model model) {
        model.addAttribute("topicsPage", topicService.listAllTopicsPage(keyword, categoryId, null, page, 10));
        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        return "admin/topics";
    }

    @GetMapping("/admin/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public String listApplications(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String sort,
                                   @RequestParam(required = false) Integer page,
                                   Model model) {
        model.addAttribute("applicationsPage", applicationService.listAllApplicationsPage(status, keyword, sort, page, 10));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort == null || sort.isBlank() ? "newest" : sort);
        return "admin/applications";
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String reports(Model model) {
        model.addAttribute("stats", dashboardStatsService.buildReportStats());
        return "admin/reports";
    }
}
