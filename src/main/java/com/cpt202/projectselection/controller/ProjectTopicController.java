package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.ApplicationForm;
import com.cpt202.projectselection.service.ProjectCategoryService;
import com.cpt202.projectselection.service.ProjectApplicationService;
import com.cpt202.projectselection.service.ProjectTopicService;
import com.cpt202.projectselection.service.TopicForm;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProjectTopicController {

    private final ProjectTopicService topicService;
    private final ProjectCategoryService categoryService;
    private final ProjectApplicationService applicationService;

    public ProjectTopicController(ProjectTopicService topicService,
                                  ProjectCategoryService categoryService,
                                  ProjectApplicationService applicationService) {
        this.topicService = topicService;
        this.categoryService = categoryService;
        this.applicationService = applicationService;
    }

    @GetMapping("/project/topics")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String teacherName,
                       @RequestParam(required = false) Integer page,
                       Model model) {
        LoginUser loginUser = CurrentUser.get();
        if (loginUser.hasRole("admin") || loginUser.hasRole("teacher")) {
            model.addAttribute("topicsPage", topicService.listAllTopicsPage(keyword, categoryId, teacherName, page, 10));
        } else {
            model.addAttribute("topicsPage", topicService.listVisibleTopicsPage(keyword, categoryId, teacherName, page, 10));
        }
        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("teacherName", teacherName);
        model.addAttribute("returnUrl", buildTopicsReturnUrl(keyword, categoryId, teacherName, page));
        return "project/topics";
    }

    @GetMapping("/project/topics/new")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("topicForm", new TopicForm());
        model.addAttribute("categories", categoryService.listActiveCategories());
        return "project/topic-form";
    }

    @PostMapping("/project/topics")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String create(@Valid @ModelAttribute TopicForm topicForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.listActiveCategories());
            return "project/topic-form";
        }
        LoginUser loginUser = CurrentUser.get();
        topicService.createTopic(topicForm, loginUser.getUserId(), loginUser.getUsername());
        redirectAttributes.addFlashAttribute("success", "Topic draft created");
        return "redirect:/project/topics";
    }

    @GetMapping("/project/topics/{topicId}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public String detail(@PathVariable Long topicId,
                         @RequestParam(required = false) String returnUrl,
                         Model model) {
        LoginUser loginUser = CurrentUser.get();
        ProjectTopic topic = topicService.getTopic(topicId);
        model.addAttribute("topic", topic);
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setTopicId(topicId);
        model.addAttribute("applicationForm", applicationForm);
        model.addAttribute("returnUrl", returnUrl == null || returnUrl.isBlank() ? "/project/topics" : returnUrl);
        model.addAttribute("hasPendingApplication", loginUser.hasRole("student")
                && applicationService.studentHasPendingApplication(loginUser.getUserId()));
        model.addAttribute("canManageTopic", topic != null
                && (loginUser.hasRole("admin") || loginUser.getUserId().equals(topic.getTeacherId())));
        return "project/topic-detail";
    }

    @GetMapping("/project/topics/{topicId}/edit")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String editForm(@PathVariable Long topicId, Model model) {
        ProjectTopic topic = topicService.getTopic(topicId);
        ensureTopicAccess(topic);
        TopicForm form = new TopicForm();
        form.setTopicId(topic.getTopicId());
        form.setTitle(topic.getTitle());
        form.setDescription(topic.getDescription());
        form.setSkills(topic.getSkills());
        form.setKeywords(topic.getKeywords());
        form.setCategoryId(topic.getCategoryId());
        form.setMaxStudents(topic.getMaxStudents());
        model.addAttribute("topicForm", form);
        model.addAttribute("categories", categoryService.listActiveCategories());
        return "project/topic-form";
    }

    @PostMapping("/project/topics/{topicId}/edit")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String edit(@PathVariable Long topicId,
                       @Valid @ModelAttribute TopicForm topicForm,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        topicForm.setTopicId(topicId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.listActiveCategories());
            return "project/topic-form";
        }
        ensureTopicAccess(topicService.getTopic(topicId));
        topicService.updateTopic(topicForm, CurrentUser.get().getUsername());
        redirectAttributes.addFlashAttribute("success", "Topic saved");
        return "redirect:/project/topics/" + topicId;
    }

    @PostMapping("/project/topics/{topicId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String publish(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        ensureTopicAccess(topicService.getTopic(topicId));
        try {
            topicService.publishTopic(topicId, CurrentUser.get().getUsername());
            redirectAttributes.addFlashAttribute("success", "Topic published");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/topics/" + topicId;
    }

    @PostMapping("/project/topics/{topicId}/close")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String close(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        ensureTopicAccess(topicService.getTopic(topicId));
        try {
            topicService.closeTopic(topicId, CurrentUser.get().getUsername());
            redirectAttributes.addFlashAttribute("success", "Topic closed");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/topics/" + topicId;
    }

    @PostMapping("/project/topics/{topicId}/archive")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String archive(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        ensureTopicAccess(topicService.getTopic(topicId));
        try {
            topicService.archiveTopic(topicId, CurrentUser.get().getUsername());
            redirectAttributes.addFlashAttribute("success", "Topic archived");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/topics/" + topicId;
    }

    private void ensureTopicAccess(ProjectTopic topic) {
        LoginUser loginUser = CurrentUser.get();
        if (topic == null || (!loginUser.hasRole("admin") && !loginUser.getUserId().equals(topic.getTeacherId()))) {
            throw new IllegalArgumentException("No permission for this topic");
        }
    }

    private String buildTopicsReturnUrl(String keyword, Long categoryId, String teacherName, Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/project/topics");
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
