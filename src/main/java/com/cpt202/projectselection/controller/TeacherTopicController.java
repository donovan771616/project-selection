package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.service.ProjectCategoryService;
import com.cpt202.projectselection.service.ProjectTopicService;
import com.cpt202.projectselection.service.TopicForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/teacher/topics")
public class TeacherTopicController {

    private final ProjectTopicService topicService;
    private final ProjectCategoryService categoryService;

    public TeacherTopicController(ProjectTopicService topicService,
                                  ProjectCategoryService categoryService) {
        this.topicService = topicService;
        this.categoryService = categoryService;
    }

    /** 列表页 */
    @GetMapping
    public String list(HttpSession session, Model model) {
        Long teacherId = (Long) session.getAttribute("userId");
        model.addAttribute("topics", topicService.listTeacherTopics(teacherId));
        return "teacher/topics";
    }

    /** 新建表单 */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("topicForm", new TopicForm());
        model.addAttribute("categories", categoryService.listActiveCategories());
        return "teacher/topic-form";
    }

    /** 提交新建 */
    @PostMapping
    public String create(@ModelAttribute TopicForm topicForm,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long teacherId = (Long) session.getAttribute("userId");
        topicService.createTopic(topicForm, teacherId);
        redirectAttributes.addFlashAttribute("success", "Topic draft created");
        return "redirect:/teacher/topics";
    }

    /** 详情页 */
    @GetMapping("/{topicId}")
    public String detail(@PathVariable Long topicId, Model model) {
        model.addAttribute("topic", topicService.getTopic(topicId));
        return "teacher/topic-detail";
    }

    /** 编辑表单 */
    @GetMapping("/{topicId}/edit")
    public String editForm(@PathVariable Long topicId, Model model) {
        ProjectTopic topic = topicService.getTopic(topicId);
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

    /** 提交编辑 */
    @PostMapping("/{topicId}/edit")
    public String edit(@PathVariable Long topicId,
                       @ModelAttribute TopicForm topicForm,
                       RedirectAttributes redirectAttributes) {
        topicForm.setTopicId(topicId);
        topicService.updateTopic(topicForm);
        redirectAttributes.addFlashAttribute("success", "Topic saved");
        return "redirect:/teacher/topics/" + topicId;
    }

    /** 发布：Draft → Open */
    @PostMapping("/{topicId}/publish")
    public String publish(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        try {
            topicService.publishTopic(topicId);
            redirectAttributes.addFlashAttribute("success", "Topic published");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/topics/" + topicId;
    }

    /** 关闭：Open → Closed */
    @PostMapping("/{topicId}/close")
    public String close(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        try {
            topicService.closeTopic(topicId);
            redirectAttributes.addFlashAttribute("success", "Topic closed");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/topics/" + topicId;
    }

    /** 归档：Closed → Archived */
    @PostMapping("/{topicId}/archive")
    public String archive(@PathVariable Long topicId, RedirectAttributes redirectAttributes) {
        try {
            topicService.archiveTopic(topicId);
            redirectAttributes.addFlashAttribute("success", "Topic archived");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/topics";
    }
}
