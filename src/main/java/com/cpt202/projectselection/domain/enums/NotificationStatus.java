package com.cpt202.projectselection.domain.enums;

/**
 * 通知状态枚举
 */
public enum NotificationStatus {
    PENDING("待处理"),
    CONFIRMED("已确认"),
    SIGNED("已签收"),
    EXPIRED("已过期"),
    CANCELLED("已取消");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
