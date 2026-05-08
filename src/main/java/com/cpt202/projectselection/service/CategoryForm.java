package com.cpt202.projectselection.service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CategoryForm {

    private Long categoryId;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    @NotNull(message = "Parent id is required")
    private Long parentId = 0L;

    @NotNull(message = "Order number is required")
    private Integer orderNum = 1;

    private String status = "0";

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
