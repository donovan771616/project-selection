package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.domain.SysRole;
import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.service.AdminUserForm;
import com.cpt202.projectselection.service.UserService;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/system/users")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String roleKey,
                       Model model) {
        model.addAttribute("users", userService.listUsers(keyword, roleKey));
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleKey", roleKey);
        return "system/users";
    }

    @GetMapping("/system/users/new")
    public String createForm(Model model) {
        AdminUserForm form = new AdminUserForm();
        form.setStatus("0");
        model.addAttribute("userForm", form);
        return "system/user-form";
    }

    @PostMapping("/system/users")
    public String create(@Valid @ModelAttribute("userForm") AdminUserForm userForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (userForm.getPassword() == null || userForm.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "password.required", "Password is required");
        }
        if (bindingResult.hasErrors()) {
            return "system/user-form";
        }
        try {
            userService.createUser(userForm, "admin");
            redirectAttributes.addFlashAttribute("success", "User created");
            return "redirect:/system/users";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("user.failed", ex.getMessage());
            return "system/user-form";
        }
    }

    @GetMapping("/system/users/{userId}/edit")
    public String editForm(@PathVariable Long userId, Model model) {
        SysUser user = userService.getUser(userId);
        AdminUserForm form = new AdminUserForm();
        form.setUserId(user.getUserId());
        form.setUserName(user.getUserName());
        form.setNickName(user.getNickName());
        form.setEmail(user.getEmail());
        form.setPhonenumber(user.getPhonenumber());
        form.setSex(user.getSex());
        form.setStatus(user.getStatus());
        form.setRoleKey(user.getRoles().stream().findFirst().map(SysRole::getRoleKey).orElse("student"));
        model.addAttribute("userForm", form);
        return "system/user-form";
    }

    @PostMapping("/system/users/{userId}/edit")
    public String edit(@PathVariable Long userId,
                       @ModelAttribute("userForm") AdminUserForm userForm,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        userForm.setUserId(userId);
        if (userForm.getNickName() == null || userForm.getNickName().isBlank()) {
            bindingResult.rejectValue("nickName", "nickName.required", "Display name is required");
        }
        if (userForm.getEmail() == null || userForm.getEmail().isBlank()) {
            bindingResult.rejectValue("email", "email.required", "Email is required");
        }
        if (bindingResult.hasErrors()) {
            return "system/user-form";
        }
        try {
            userService.updateUser(userForm, "admin");
            redirectAttributes.addFlashAttribute("success", "User saved");
            return "redirect:/system/users";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("user.failed", ex.getMessage());
            return "system/user-form";
        }
    }
}
