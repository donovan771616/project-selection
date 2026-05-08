package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectCategory;
import com.cpt202.projectselection.mapper.ProjectCategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCategoryServiceTest {

    @Mock
    private ProjectCategoryMapper categoryMapper;

    private ProjectCategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new ProjectCategoryService(categoryMapper);
    }

    @Test
    void createCategorySetsCreateBy() {
        CategoryForm form = new CategoryForm();
        form.setCategoryName("Web Development");
        form.setParentId(0L);
        form.setOrderNum(1);

        categoryService.createCategory(form, "admin");

        ArgumentCaptor<ProjectCategory> captor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(categoryMapper).insertCategory(captor.capture());
        assertThat(captor.getValue().getCategoryName()).isEqualTo("Web Development");
        assertThat(captor.getValue().getCreateBy()).isEqualTo("admin");
    }

    @Test
    void updateCategoryRejectsSelfParent() {
        CategoryForm form = new CategoryForm();
        form.setCategoryId(5L);
        form.setCategoryName("Web Development");
        form.setParentId(5L);

        assertThatThrownBy(() -> categoryService.updateCategory(form, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A category cannot be its own parent");
    }

    @Test
    void updateCategoryCopiesAllFields() {
        CategoryForm form = new CategoryForm();
        form.setCategoryId(5L);
        form.setCategoryName("Updated Name");
        form.setParentId(1L);
        form.setOrderNum(3);
        form.setStatus("1");

        categoryService.updateCategory(form, "admin");

        ArgumentCaptor<ProjectCategory> captor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(categoryMapper).updateCategory(captor.capture());
        ProjectCategory updated = captor.getValue();
        assertThat(updated.getCategoryId()).isEqualTo(5L);
        assertThat(updated.getCategoryName()).isEqualTo("Updated Name");
        assertThat(updated.getParentId()).isEqualTo(1L);
        assertThat(updated.getOrderNum()).isEqualTo(3);
        assertThat(updated.getUpdateBy()).isEqualTo("admin");
    }

    @Test
    void deleteCategoryRejectsCategoryInUse() {
        when(categoryMapper.countTopicsByCategoryId(5L)).thenReturn(3);

        assertThatThrownBy(() -> categoryService.deleteCategory(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This category is used by topics and cannot be deleted");
    }

    @Test
    void deleteCategorySucceedsForUnusedCategory() {
        when(categoryMapper.countTopicsByCategoryId(5L)).thenReturn(0);

        categoryService.deleteCategory(5L);

        verify(categoryMapper).deleteCategory(5L);
    }

    @Test
    void listCategoryTreeItemsBuildsTreeFromFlatList() {
        ProjectCategory root = new ProjectCategory();
        root.setCategoryId(1L);
        root.setCategoryName("Root");
        root.setParentId(0L);

        ProjectCategory child = new ProjectCategory();
        child.setCategoryId(2L);
        child.setCategoryName("Child");
        child.setParentId(1L);

        when(categoryMapper.selectAllCategories()).thenReturn(Arrays.asList(root, child));

        List<CategoryTreeItem> tree = categoryService.listCategoryTreeItems();

        assertThat(tree).hasSize(2);
        assertThat(tree.get(0).getCategory().getCategoryName()).isEqualTo("Root");
        assertThat(tree.get(0).getDepth()).isEqualTo(0);
        assertThat(tree.get(1).getCategory().getCategoryName()).isEqualTo("Child");
        assertThat(tree.get(1).getDepth()).isEqualTo(1);
    }

    @Test
    void listActiveCategoriesReturnsOnlyActive() {
        ProjectCategory active = new ProjectCategory();
        active.setCategoryId(1L);
        active.setStatus("1");

        when(categoryMapper.selectActiveCategories()).thenReturn(Arrays.asList(active));

        List<ProjectCategory> result = categoryService.listActiveCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("1");
    }
}
