package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.ProjectCategory;
import com.cpt202.projectselection.service.CategoryForm;
import com.cpt202.projectselection.service.ProjectCategoryService;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProjectCategoryController {

    private final ProjectCategoryService categoryService;

    public ProjectCategoryController(ProjectCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/project/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.listAllCategories());
        model.addAttribute("categoryTree", categoryService.listCategoryTreeItems());
        model.addAttribute("categoryForm", new CategoryForm());
        return "project/categories";
    }

    @PostMapping("/project/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute CategoryForm categoryForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.listAllCategories());
            model.addAttribute("categoryTree", categoryService.listCategoryTreeItems());
            return "project/categories";
        }
        categoryService.createCategory(categoryForm, CurrentUser.get().getUsername());
        redirectAttributes.addFlashAttribute("success", "Category created");
        return "redirect:/project/categories";
    }

    @GetMapping("/project/categories/{categoryId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long categoryId, Model model) {
        ProjectCategory category = categoryService.getCategory(categoryId);
        CategoryForm form = new CategoryForm();
        form.setCategoryId(category.getCategoryId());
        form.setCategoryName(category.getCategoryName());
        form.setParentId(category.getParentId());
        form.setOrderNum(category.getOrderNum());
        form.setStatus(category.getStatus());
        model.addAttribute("categoryForm", form);
        model.addAttribute("categories", categoryService.listAllCategories());
        model.addAttribute("categoryTree", categoryService.listCategoryTreeItems());
        return "project/categories";
    }

    @PostMapping("/project/categories/{categoryId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String edit(@PathVariable Long categoryId,
                       @Valid @ModelAttribute CategoryForm categoryForm,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        categoryForm.setCategoryId(categoryId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.listAllCategories());
            model.addAttribute("categoryTree", categoryService.listCategoryTreeItems());
            return "project/categories";
        }
        try {
            categoryService.updateCategory(categoryForm, CurrentUser.get().getUsername());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("category.failed", ex.getMessage());
            model.addAttribute("categories", categoryService.listAllCategories());
            model.addAttribute("categoryTree", categoryService.listCategoryTreeItems());
            return "project/categories";
        }
        redirectAttributes.addFlashAttribute("success", "Category saved");
        return "redirect:/project/categories";
    }

    @PostMapping("/project/categories/{categoryId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long categoryId, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("success", "Category deleted");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/project/categories";
    }
}
