package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.ApplicationForm;
import com.cpt202.projectselection.service.BatchReviewResult;
import com.cpt202.projectselection.service.ProjectApplicationService;
import com.cpt202.projectselection.service.RejectForm;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProjectApplicationController {

    private final ProjectApplicationService applicationService;

    public ProjectApplicationController(ProjectApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/project/applications")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','STUDENT')")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer pendingPage,
                       @RequestParam(required = false) Integer historyPage,
                       Model model) {
        LoginUser loginUser = CurrentUser.get();
        if (loginUser.hasRole("admin")) {
            model.addAttribute("applicationsPage", applicationService.listAllApplicationsPage(status, keyword, sort, page, 10));
        } else if (loginUser.hasRole("teacher")) {
            model.addAttribute("pendingApplicationsPage",
                    applicationService.listTeacherPendingPage(loginUser.getUserId(), keyword, sort, null, pendingPage, 10));
            model.addAttribute("historyApplicationsPage",
                    applicationService.listTeacherHistoryPage(loginUser.getUserId(), status, keyword, sort, null, historyPage, 10));
        } else {
            model.addAttribute("applicationsPage",
                    applicationService.listStudentHistoryPage(loginUser.getUserId(), status, keyword, sort, page, 10));
        }
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort == null || sort.isBlank() ? "newest" : sort);
        model.addAttribute("rejectForm", new RejectForm());
        return "project/applications";
    }

    @GetMapping("/project/applications/{applicationId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','STUDENT')")
    public String detail(@PathVariable Long applicationId, Model model) {
        LoginUser loginUser = CurrentUser.get();
        ProjectApplication application = requireApplicationAccess(applicationId, loginUser);

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
        model.addAttribute("canWithdraw", application.getStatus().equals("Pending")
                && loginUser.hasRole("student") && loginUser.getUserId().equals(application.getStudentId()));
        model.addAttribute("canDelete", loginUser.hasRole("admin"));
        model.addAttribute("rejectForm", new RejectForm());
        return "project/application-detail";
    }

    @PostMapping("/project/topics/{topicId}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public String apply(@PathVariable Long topicId,
                        @Valid @ModelAttribute ApplicationForm applicationForm,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Application note must be within 500 characters");
            return "redirect:/project/topics/" + topicId;
        }
        LoginUser loginUser = CurrentUser.get();
        try {
            applicationService.submitApplication(topicId, loginUser.getUserId(),
                    applicationForm.getPersonalNote(), loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Application submitted");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/topics/" + topicId;
    }

    @PostMapping("/project/applications/{applicationId}/accept")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String accept(@PathVariable Long applicationId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        Long teacherId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        try {
            applicationService.acceptApplication(applicationId, teacherId, loginUser.getUserId(), loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Application accepted");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/applications";
    }

    @PostMapping("/project/applications/batch-accept")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String batchAccept(@RequestParam(name = "applicationIds", required = false) List<Long> applicationIds,
                              RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        Long teacherId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        BatchReviewResult result = applicationService.batchAcceptApplications(
                applicationIds, teacherId, loginUser.getUserId(), loginUser.getUsername());
        addBatchFlash(result, redirectAttributes);
        return "redirect:/project/applications";
    }

    @PostMapping("/project/applications/batch-reject")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String batchReject(@RequestParam(name = "applicationIds", required = false) List<Long> applicationIds,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        if (reason == null || reason.length() < 10 || reason.length() > 200) {
            redirectAttributes.addFlashAttribute("error", "Reason must be 10-200 characters");
            return "redirect:/project/applications";
        }
        LoginUser loginUser = CurrentUser.get();
        Long teacherId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        BatchReviewResult result = applicationService.batchRejectApplications(
                applicationIds, teacherId, loginUser.getUserId(), reason, loginUser.getUsername());
        addBatchFlash(result, redirectAttributes);
        return "redirect:/project/applications";
    }

    @PostMapping("/project/applications/{applicationId}/reject")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String reject(@PathVariable Long applicationId,
                         @Valid @ModelAttribute RejectForm rejectForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Reason must be 10-200 characters");
            return "redirect:/project/applications";
        }
        LoginUser loginUser = CurrentUser.get();
        Long teacherId = loginUser.hasRole("admin") ? 0L : loginUser.getUserId();
        try {
            applicationService.rejectApplication(applicationId, teacherId, loginUser.getUserId(),
                    rejectForm.getReason(), loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Application rejected");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/applications";
    }

    @PostMapping("/project/applications/{applicationId}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public String withdraw(@PathVariable Long applicationId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        try {
            applicationService.withdrawApplication(applicationId, loginUser.getUserId(), loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Application withdrawn");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/applications";
    }

    @PostMapping("/admin/applications/{applicationId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDelete(@PathVariable Long applicationId, RedirectAttributes redirectAttributes) {
        try {
            applicationService.deleteApplication(applicationId, CurrentUser.get().getUsername());
            redirectAttributes.addFlashAttribute("success", "Application deleted");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/applications";
    }

    private ProjectApplication requireApplicationAccess(Long applicationId, LoginUser loginUser) {
        ProjectApplication application = applicationService.getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application does not exist");
        }
        boolean allowed = loginUser.hasRole("admin")
                || (loginUser.hasRole("teacher") && loginUser.getUserId().equals(application.getTeacherId()))
                || (loginUser.hasRole("student") && loginUser.getUserId().equals(application.getStudentId()));
        if (!allowed) {
            throw new IllegalArgumentException("No permission for this application");
        }
        return application;
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
