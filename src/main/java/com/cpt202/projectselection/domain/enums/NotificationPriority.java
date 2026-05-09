package com.cpt202.projectselection.domain.enums;

/**
 * 通知优先级枚举
 */
public enum NotificationPriority {
    HIGH("高"),
    NORMAL("普通"),
    LOW("低");

    private final String description;

    NotificationPriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
