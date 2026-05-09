package com.cpt202.projectselection.domain;

import java.time.LocalDateTime;

public class ProjectApplication extends BaseEntity {

    private Long applicationId;
    private Long topicId;
    private String topicTitle;
    private String topicDescription;
    private String topicSkills;
    private String topicCategoryName;
    private String topicStatus;
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String personalNote;
    private String status;
    private String rejectReason;
    private Long rejectBy;
    private LocalDateTime rejectTime;
    private String delFlag;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getTopicDescription() {
        return topicDescription;
    }

    public void setTopicDescription(String topicDescription) {
        this.topicDescription = topicDescription;
    }

    public String getTopicSkills() {
        return topicSkills;
    }

    public void setTopicSkills(String topicSkills) {
        this.topicSkills = topicSkills;
    }

    public String getTopicCategoryName() {
        return topicCategoryName;
    }

    public void setTopicCategoryName(String topicCategoryName) {
        this.topicCategoryName = topicCategoryName;
    }

    public String getTopicStatus() {
        return topicStatus;
    }

    public void setTopicStatus(String topicStatus) {
        this.topicStatus = topicStatus;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getPersonalNote() {
        return personalNote;
    }

    public void setPersonalNote(String personalNote) {
        this.personalNote = personalNote;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Long getRejectBy() {
        return rejectBy;
    }

    public void setRejectBy(Long rejectBy) {
        this.rejectBy = rejectBy;
    }

    public LocalDateTime getRejectTime() {
        return rejectTime;
    }

    public void setRejectTime(LocalDateTime rejectTime) {
        this.rejectTime = rejectTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }
}
