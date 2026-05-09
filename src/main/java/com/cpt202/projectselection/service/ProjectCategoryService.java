package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectCategory;
import com.cpt202.projectselection.mapper.ProjectCategoryMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectCategoryService {

    private final ProjectCategoryMapper categoryMapper;

    public ProjectCategoryService(ProjectCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<ProjectCategory> listActiveCategories() {
        return categoryMapper.selectActiveCategories();
    }

    public List<ProjectCategory> listAllCategories() {
        return categoryMapper.selectAllCategories();
    }

    public List<CategoryTreeItem> listCategoryTreeItems() {
        List<ProjectCategory> categories = categoryMapper.selectAllCategories();
        Map<Long, List<ProjectCategory>> children = new HashMap<>();
        for (ProjectCategory category : categories) {
            children.computeIfAbsent(category.getParentId(), key -> new ArrayList<>()).add(category);
        }
        List<CategoryTreeItem> tree = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        appendChildren(0L, 0, children, tree, visited);
        for (ProjectCategory category : categories) {
            if (!visited.contains(category.getCategoryId())) {
                appendCategory(category, 0, children, tree, visited);
            }
        }
        return tree;
    }

    public ProjectCategory getCategory(Long categoryId) {
        return categoryMapper.selectCategoryById(categoryId);
    }

    public void createCategory(CategoryForm form, String operator) {
        ProjectCategory category = new ProjectCategory();
        copyForm(form, category);
        category.setCreateBy(operator);
        categoryMapper.insertCategory(category);
    }

    public void updateCategory(CategoryForm form, String operator) {
        if (form.getCategoryId() != null && form.getCategoryId().equals(form.getParentId())) {
            throw new IllegalArgumentException("A category cannot be its own parent");
        }
        ProjectCategory category = new ProjectCategory();
        copyForm(form, category);
        category.setUpdateBy(operator);
        categoryMapper.updateCategory(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        if (categoryMapper.countTopicsByCategoryId(categoryId) > 0) {
            throw new IllegalArgumentException("This category is used by topics and cannot be deleted");
        }
        categoryMapper.deleteCategory(categoryId);
    }

    private void copyForm(CategoryForm form, ProjectCategory category) {
        category.setCategoryId(form.getCategoryId());
        category.setCategoryName(form.getCategoryName());
        category.setParentId(form.getParentId());
        category.setOrderNum(form.getOrderNum());
        category.setStatus(form.getStatus());
    }

    private void appendChildren(Long parentId, int depth, Map<Long, List<ProjectCategory>> children,
                                List<CategoryTreeItem> tree, Set<Long> visited) {
        for (ProjectCategory child : children.getOrDefault(parentId, List.of())) {
            appendCategory(child, depth, children, tree, visited);
        }
    }

    private void appendCategory(ProjectCategory category, int depth, Map<Long, List<ProjectCategory>> children,
                                List<CategoryTreeItem> tree, Set<Long> visited) {
        if (!visited.add(category.getCategoryId())) {
            return;
        }
        tree.add(new CategoryTreeItem(category, depth));
        appendChildren(category.getCategoryId(), depth + 1, children, tree, visited);
    }
}
