package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.BatchReviewResult;
import com.cpt202.projectselection.service.NotificationService;
import com.cpt202.projectselection.service.ProjectApplicationService;
import com.cpt202.projectselection.service.ProjectTopicService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherApplicationController {

    private final ProjectApplicationService applicationService;
    private final NotificationService notificationService;
    private final ProjectTopicService topicService;

    public TeacherApplicationController(ProjectApplicationService applicationService,
                                     NotificationService notificationService,
                                     ProjectTopicService topicService) {
        this.applicationService = applicationService;
        this.notificationService = notificationService;
        this.topicService = topicService;
    }

    @GetMapping("/teacher/applications")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) Long topicId,
                       @RequestParam(required = false) Integer pendingPage,
                       @RequestParam(required = false) Integer historyPage,
                       Model model) {
        LoginUser loginUser = CurrentUser.get();
        List<ProjectTopic> teacherTopics = topicService.listTeacherTopics(loginUser.getUserId());
        model.addAttribute("teacherTopics", teacherTopics);
        model.addAttribute("pendingApplicationsPage",
                applicationService.listTeacherPendingPage(loginUser.getUserId(), keyword, sort, topicId, pendingPage, 10));
        model.addAttribute("historyApplicationsPage",
                applicationService.listTeacherHistoryPage(loginUser.getUserId(), status, keyword, sort, topicId, historyPage, 10));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort == null || sort.isBlank() ? "newest" : sort);
        model.addAttribute("topicId", topicId);
        return "teacher/applications";
    }

    @GetMapping("/teacher/applications/{applicationId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String detail(@PathVariable Long applicationId, Model model) {
        LoginUser loginUser = CurrentUser.get();
        ProjectApplication application = applicationService.getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application does not exist");
        }
        boolean allowed = loginUser.hasRole("admin")
                || (loginUser.hasRole("teacher") && loginUser.getUserId().equals(application.getTeacherId()));
        if (!allowed) {
            throw new IllegalArgumentException("No permission for this application");
        }

        Map<String, Object> appData = new LinkedHashMap<>();
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
        model.addAttribute("canReview", application.getStatus().equals("Pending")
                && (loginUser.hasRole("admin") || loginUser.getUserId().equals(application.getTeacherId())));
        return "teacher/application-detail";
    }

    @PostMapping("/teacher/applications/{applicationId}/accept")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String accept(@PathVariable Long applicationId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        ProjectApplication application = applicationService.getApplication(applicationId);
        if (application == null) {
            redirectAttributes.addFlashAttribute("error", "Application does not exist");
            return "redirect:/teacher/applications";
        }
        if (!"Pending".equals(application.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Only pending applications can be accepted");
            return "redirect:/teacher/applications";
        }
        if (!loginUser.hasRole("admin") && !loginUser.getUserId().equals(application.getTeacherId())) {
            redirectAttributes.addFlashAttribute("error", "No permission for this application");
            return "redirect:/teacher/applications";
        }
        Long reviewerId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        try {
            applicationService.acceptApplication(applicationId, reviewerId, loginUser.getUserId(), loginUser.getUsername());
            notificationService.sendApplicationApprovedNotification(applicationId, loginUser.getUserId(), loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Application accepted");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/applications";
    }

    @PostMapping("/teacher/applications/batch-accept")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String batchAccept(@RequestParam(name = "applicationIds", required = false) List<Long> applicationIds,
                              RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        Long reviewerId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        BatchReviewResult result = applicationService.batchAcceptApplications(
                applicationIds, reviewerId, loginUser.getUserId(), loginUser.getUsername());
        for (int i = 0; i < applicationIds.size(); i++) {
            Long appId = applicationIds.get(i);
            if (result.getSuccessIds().contains(appId)) {
                notificationService.sendApplicationApprovedNotification(appId, loginUser.getUserId(), loginUser.getUsername());
            }
        }
        addBatchFlash(result, redirectAttributes);
        return "redirect:/teacher/applications";
    }

    @PostMapping("/teacher/applications/batch-reject")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String batchReject(@RequestParam(name = "applicationIds", required = false) List<Long> applicationIds,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        if (reason == null || reason.length() < 10 || reason.length() > 200) {
            redirectAttributes.addFlashAttribute("error", "Reason must be 10-200 characters");
            return "redirect:/teacher/applications";
        }
        LoginUser loginUser = CurrentUser.get();
        Long reviewerId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        BatchReviewResult result = applicationService.batchRejectApplications(
                applicationIds, reviewerId, loginUser.getUserId(), reason, loginUser.getUsername());
        for (int i = 0; i < applicationIds.size(); i++) {
            Long appId = applicationIds.get(i);
            if (result.getSuccessIds().contains(appId)) {
                notificationService.sendApplicationRejectedNotification(appId, loginUser.getUserId(),
                        loginUser.getUsername(), reason);
            }
        }
        addBatchFlash(result, redirectAttributes);
        return "redirect:/teacher/applications";
    }

    @PostMapping("/teacher/applications/{applicationId}/reject")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String reject(@PathVariable Long applicationId,
                          @RequestParam String reason,
                          RedirectAttributes redirectAttributes) {
        if (reason == null || reason.length() < 10 || reason.length() > 200) {
            redirectAttributes.addFlashAttribute("error", "Reason must be 10-200 characters");
            return "redirect:/teacher/applications";
        }
        LoginUser loginUser = CurrentUser.get();
        ProjectApplication application = applicationService.getApplication(applicationId);
        if (application == null) {
            redirectAttributes.addFlashAttribute("error", "Application does not exist");
            return "redirect:/teacher/applications";
        }
        if (!"Pending".equals(application.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Only pending applications can be rejected");
            return "redirect:/teacher/applications";
        }
        if (!loginUser.hasRole("admin") && !loginUser.getUserId().equals(application.getTeacherId())) {
            redirectAttributes.addFlashAttribute("error", "No permission for this application");
            return "redirect:/teacher/applications";
        }
        Long reviewerId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        try {
            applicationService.rejectApplication(applicationId, reviewerId, loginUser.getUserId(),
                                                  reason, loginUser.getUsername());
            notificationService.sendApplicationRejectedNotification(applicationId, loginUser.getUserId(),
                    loginUser.getUsername(), reason);
            redirectAttributes.addFlashAttribute("success", "Application rejected");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/applications";
    }

    private void addBatchFlash(BatchReviewResult result, RedirectAttributes redirectAttributes) {
        if (result.getSuccessCount() > 0) {
            redirectAttributes.addFlashAttribute("success", result.summary());
        }
        if (result.getFailureCount() > 0) {
            redirectAttributes.addFlashAttribute("error", String.join("; ", result.getFailures()));
        }
    }
}
