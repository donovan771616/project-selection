package com.cpt202.projectselection.domain.enums;

/**
 * 发送状态枚举
 */
public enum SendStatus {
    PENDING("待发送"),
    SENT("已发送"),
    FAILED("失败"),
    PARTIAL_FAILED("部分失败");

    private final String description;

    SendStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
