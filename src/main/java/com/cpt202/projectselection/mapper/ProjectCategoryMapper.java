package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.ProjectCategory;
import java.util.List;

public interface ProjectCategoryMapper {

    ProjectCategory selectCategoryById(Long categoryId);

    List<ProjectCategory> selectActiveCategories();

    List<ProjectCategory> selectAllCategories();

    int insertCategory(ProjectCategory category);

    int updateCategory(ProjectCategory category);

    int deleteCategory(Long categoryId);

    int countTopicsByCategoryId(Long categoryId);
}
