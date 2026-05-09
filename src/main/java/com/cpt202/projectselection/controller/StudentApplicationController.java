package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.ProjectApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class StudentApplicationController {

    private final ProjectApplicationService applicationService;

    public StudentApplicationController(ProjectApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/student/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) Integer page,
                       Model model) {
        LoginUser loginUser = CurrentUser.get();
        model.addAttribute("applicationsPage",
                applicationService.listStudentHistoryPage(loginUser.getUserId(), status, keyword, sort, page, 10));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort == null || sort.isBlank() ? "newest" : sort);
        return "student/applications";
    }

    @GetMapping("/student/applications/{applicationId}")
    @PreAuthorize("hasRole('STUDENT')")
    public String detail(@PathVariable Long applicationId, Model model) {
        LoginUser loginUser = CurrentUser.get();
        ProjectApplication application = requireApplicationAccess(applicationId, loginUser);
        
        Map<String, Object> appData = new HashMap<>();
        appData.put("applicationId", application.getApplicationId());
        appData.put("topicId", application.getTopicId());
        appData.put("topicTitle", application.getTopicTitle());
        appData.put("topicDescription", application.getTopicDescription());
        appData.put("topicSkills", application.getTopicSkills());
        appData.put("topicCategoryName", application.getTopicCategoryName());
        appData.put("topicStatus", application.getTopicStatus());
        appData.put("teacherId", application.getTeacherId());
        appData.put("teacherName", application.getTeacherName());
        appData.put("studentId", application.getStudentId());
        appData.put("studentName", application.getStudentName());
        appData.put("studentEmail", application.getStudentEmail());
        appData.put("personalNote", application.getPersonalNote());
        appData.put("status", application.getStatus());
        appData.put("rejectReason", application.getRejectReason());
        appData.put("createTime", application.getCreateTime());
        appData.put("updateTime", application.getUpdateTime());
        
        model.addAttribute("appData", appData);
        model.addAttribute("logs", applicationService.getApplicationLogs(applicationId));
        model.addAttribute("canWithdraw", application.getStatus().equals("Pending")
                && loginUser.getUserId().equals(application.getStudentId()));
        return "student/application-detail";
    }
    
    @GetMapping("/student/applications/{applicationId}/api")
    @ResponseBody
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> detailApi(@PathVariable Long applicationId) {
        LoginUser loginUser = CurrentUser.get();
        ProjectApplication application = requireApplicationAccess(applicationId, loginUser);
        
        Map<String, Object> appData = new HashMap<>();
        appData.put("applicationId", application.getApplicationId());
        appData.put("topicId", application.getTopicId());
        appData.put("topicTitle", application.getTopicTitle());
        appData.put("topicDescription", application.getTopicDescription());
        appData.put("topicSkills", application.getTopicSkills());
        appData.put("topicCategoryName", application.getTopicCategoryName());
        appData.put("topicStatus", application.getTopicStatus());
        appData.put("teacherId", application.getTeacherId());
        appData.put("teacherName", application.getTeacherName());
        appData.put("studentId", application.getStudentId());
        appData.put("studentName", application.getStudentName());
        appData.put("studentEmail", application.getStudentEmail());
        appData.put("personalNote", application.getPersonalNote());
        appData.put("status", application.getStatus());
        appData.put("rejectReason", application.getRejectReason());
        appData.put("createTime", application.getCreateTime());
        appData.put("updateTime", application.getUpdateTime());
        
        return appData;
    }

    @PostMapping("/student/applications/{applicationId}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public String withdraw(@PathVariable Long applicationId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        try {
            applicationService.withdrawApplication(applicationId, loginUser.getUserId(), loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Application withdrawn");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/applications";
    }

    private ProjectApplication requireApplicationAccess(Long applicationId, LoginUser loginUser) {
        ProjectApplication application = applicationService.getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application does not exist");
        }
        if (!loginUser.getUserId().equals(application.getStudentId())) {
            throw new IllegalArgumentException("No permission for this application");
        }
        return application;
    }
}
