package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.StatItem;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.ProjectCategoryService;
import com.cpt202.projectselection.service.ProjectTopicService;
import com.cpt202.projectselection.service.TopicForm;
import java.util.HashMap;
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
public class TeacherTopicController {

    private final ProjectTopicService topicService;
    private final ProjectCategoryService categoryService;
    private final ProjectApplicationMapper applicationMapper;

    public TeacherTopicController(ProjectTopicService topicService,
                                  ProjectCategoryService categoryService,
                                  ProjectApplicationMapper applicationMapper) {
        this.topicService = topicService;
        this.categoryService = categoryService;
        this.applicationMapper = applicationMapper;
    }

    @GetMapping("/teacher/topics")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Integer page,
                       Model model) {
        LoginUser loginUser = CurrentUser.get();
        model.addAttribute("topicsPage", topicService.listTeacherTopicsPage(
                loginUser.getUserId(), keyword, categoryId, status, page, 10));
        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        
        List<StatItem> pendingCounts = applicationMapper.selectPendingCountByTeacherTopics(loginUser.getUserId());
        Map<String, Integer> pendingCountMap = new HashMap<>();
        for (StatItem item : pendingCounts) {
            pendingCountMap.put(item.getLabel(), item.getValue());
        }
        model.addAttribute("pendingCountMap", pendingCountMap);
        
        return "teacher/topics";
    }

    @GetMapping("/teacher/topics/new")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("topicForm", new TopicForm());
        model.addAttribute("categories", categoryService.listActiveCategories());
        return "teacher/topic-form";
    }

    @PostMapping("/teacher/topics")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String create(@Valid @ModelAttribute TopicForm topicForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.listActiveCategories());
            return "teacher/topic-form";
        }
        LoginUser loginUser = CurrentUser.get();
        topicService.createTopic(topicForm, loginUser.getUserId(), loginUser.getUsername());
        redirectAttributes.addFlashAttribute("success", "Topic draft created");
        return "redirect:/teacher/topics";
    }

    @GetMapping("/teacher/topics/{topicId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String detail(@PathVariable Long topicId, Model model) {
        LoginUser loginUser = CurrentUser.get();
        ProjectTopic topic = topicService.getTopic(topicId);
        ensureTopicAccess(topic, loginUser);
        model.addAttribute("topic", topic);
        model.addAttribute("canManage", true);
        model.addAttribute("pendingCount", applicationMapper.countPendingByTopicId(topicId));
        return "teacher/topic-detail";
    }

    @GetMapping("/teacher/topics/{topicId}/edit")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String editForm(@PathVariable Long topicId, Model model) {
        LoginUser loginUser = CurrentUser.get();
        ProjectTopic topic = topicService.getTopic(topicId);
        ensureTopicAccess(topic, loginUser);
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
        return "teacher/topic-form";
    }

    @PostMapping("/teacher/topics/{topicId}/edit")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String edit(@PathVariable Long topicId,
                       @Valid @ModelAttribute TopicForm topicForm,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        topicForm.setTopicId(topicId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.listActiveCategories());
            return "teacher/topic-form";
        }
        ensureTopicAccess(topicService.getTopic(topicId), loginUser);
        topicService.updateTopic(topicForm, loginUser.getUsername());
        redirectAttributes.addFlashAttribute("success", "Topic saved");
        return "redirect:/teacher/topics/" + topicId;
    }

    @PostMapping("/teacher/topics/{topicId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String publish(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        ProjectTopic topic = topicService.getTopic(topicId);
        ensureTopicAccess(topic, loginUser);
        try {
            topicService.publishTopic(topicId, loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Topic published");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/topics/" + topicId;
    }

    @PostMapping("/teacher/topics/{topicId}/close")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String close(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        ProjectTopic topic = topicService.getTopic(topicId);
        ensureTopicAccess(topic, loginUser);
        try {
            topicService.closeTopic(topicId, loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Topic closed");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/topics/" + topicId;
    }

    @PostMapping("/teacher/topics/{topicId}/archive")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public String archive(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        ProjectTopic topic = topicService.getTopic(topicId);
        ensureTopicAccess(topic, loginUser);
        try {
            topicService.archiveTopic(topicId, loginUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Topic archived");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/topics";
    }

    private void ensureTopicAccess(ProjectTopic topic, LoginUser loginUser) {
        if (topic == null || (!loginUser.hasRole("admin") && !loginUser.getUserId().equals(topic.getTeacherId()))) {
            throw new IllegalArgumentException("No permission for this topic");
        }
    }
}
