package com.cpt202.projectselection.service;

import javax.validation.constraints.Size;

public class ApplicationForm {

    private Long topicId;

    @Size(max = 500, message = "Note must be within 500 characters")
    private String personalNote;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getPersonalNote() {
        return personalNote;
    }

    public void setPersonalNote(String personalNote) {
        this.personalNote = personalNote;
    }
}
