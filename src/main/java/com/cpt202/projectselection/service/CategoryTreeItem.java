package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectCategory;

public class CategoryTreeItem {

    private final ProjectCategory category;
    private final int depth;

    public CategoryTreeItem(ProjectCategory category, int depth) {
        this.category = category;
        this.depth = depth;
    }

    public ProjectCategory getCategory() {
        return category;
    }

    public int getDepth() {
        return depth;
    }

    public int getIndent() {
        return depth * 22;
    }
}
