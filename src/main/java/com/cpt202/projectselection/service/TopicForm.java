package com.cpt202.projectselection.service;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TopicForm {

    private Long topicId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be within 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 500, message = "Skills must be within 500 characters")
    private String skills;

    @Size(max = 200, message = "Keywords must be within 200 characters")
    private String keywords;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Min(value = 1, message = "Max students must be at least 1")
    private Integer maxStudents = 1;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }
}
