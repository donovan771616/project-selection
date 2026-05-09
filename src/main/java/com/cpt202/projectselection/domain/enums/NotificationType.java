package com.cpt202.projectselection.domain.enums;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    APPLICATION_APPROVED("申请通过"),
    APPLICATION_REJECTED("申请拒绝"),
    CONFIRM_REMINDER("确认提醒"),
    OVERDUE_REMINDER("逾期提醒"),
    REVIEW_REMINDER("审核提醒"),
    PUSH_FAILED("推送失败");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
