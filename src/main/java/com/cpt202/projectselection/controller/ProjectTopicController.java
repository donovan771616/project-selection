package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.service.ProjectCategoryService;
import com.cpt202.projectselection.service.ProjectTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/project/topics")
public class ProjectTopicController {

    private final ProjectTopicService topicService;
    private final ProjectCategoryService categoryService;

    public ProjectTopicController(ProjectTopicService topicService,
                                  ProjectCategoryService categoryService) {
        this.topicService = topicService;
        this.categoryService = categoryService;
    }

    /** 学生浏览所有 Open 题目 */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("topics", topicService.listOpenTopics());
        model.addAttribute("categories", categoryService.listActiveCategories());
        return "project/topics";
    }

    /** 题目详情 */
    @GetMapping("/{topicId}")
    public String detail(@PathVariable Long topicId, Model model) {
        model.addAttribute("topic", topicService.getTopic(topicId));
        return "project/topic-detail";
    }
}
