package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.ApplicationForm;
import com.cpt202.projectselection.service.ProjectApplicationService;
import com.cpt202.projectselection.service.ProjectCategoryService;
import com.cpt202.projectselection.service.ProjectTopicService;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentTopicController {

    private final ProjectTopicService topicService;
    private final ProjectCategoryService categoryService;
    private final ProjectApplicationService applicationService;

    public StudentTopicController(ProjectTopicService topicService,
                                   ProjectCategoryService categoryService,
                                   ProjectApplicationService applicationService) {
        this.topicService = topicService;
        this.categoryService = categoryService;
        this.applicationService = applicationService;
    }

    @GetMapping("/student/topics")
    @PreAuthorize("hasRole('STUDENT')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String teacherName,
                       @RequestParam(required = false) Integer page,
                       Model model) {
        LoginUser loginUser = CurrentUser.get();
        model.addAttribute("topicsPage", topicService.listVisibleTopicsPage(keyword, categoryId, teacherName, page, 10));
        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("teacherName", teacherName);
        model.addAttribute("returnUrl", buildTopicsReturnUrl(keyword, categoryId, teacherName, page));
        model.addAttribute("hasPendingApplication", applicationService.studentHasPendingApplication(loginUser.getUserId()));
        return "student/topics";
    }

    @GetMapping("/student/topics/{topicId}")
    @PreAuthorize("hasRole('STUDENT')")
    public String detail(@PathVariable Long topicId,
                          @RequestParam(required = false) String returnUrl,
                          Model model) {
        LoginUser loginUser = CurrentUser.get();
        ProjectTopic topic = topicService.getTopic(topicId);
        if (topic == null) {
            model.addAttribute("error", "This topic is not available");
            return "student/topic-detail";
        }
        model.addAttribute("topic", topic);
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setTopicId(topicId);
        model.addAttribute("applicationForm", applicationForm);
        model.addAttribute("returnUrl", returnUrl == null || returnUrl.isBlank() ? "/student/topics" : returnUrl);
        model.addAttribute("hasPendingApplication", applicationService.studentHasPendingApplication(loginUser.getUserId()));
        return "student/topic-detail";
    }

    @PostMapping("/student/topics/{topicId}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public String apply(@PathVariable Long topicId,
                        @Valid @ModelAttribute ApplicationForm applicationForm,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Application note must be within 500 characters");
            return "redirect:/student/topics/" + topicId;
        }
        LoginUser loginUser = CurrentUser.get();
        try {
            applicationService.submitApplication(topicId, loginUser.getUserId(),
                    applicationForm.getPersonalNote(), loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Application submitted");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/topics/" + topicId;
    }

    private String buildTopicsReturnUrl(String keyword, Long categoryId, String teacherName, Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/student/topics");
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }
        if (categoryId != null) {
            builder.queryParam("categoryId", categoryId);
        }
        if (teacherName != null && !teacherName.isBlank()) {
            builder.queryParam("teacherName", teacherName);
        }
        if (page != null && page > 1) {
            builder.queryParam("page", page);
        }
        return builder.build().toUriString();
    }
}
