package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.ProfileForm;
import com.cpt202.projectselection.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        LoginUser loginUser = CurrentUser.get();
        SysUser user = userService.getUser(loginUser.getUserId());
        ProfileForm form = createProfileForm(user);

        String roleKey = loginUser.primaryRole();
        model.addAttribute("profileForm", form);
        model.addAttribute("roleKey", roleKey);
        model.addAttribute("userInfo", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileForm profileForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        LoginUser loginUser = CurrentUser.get();
        String roleKey = loginUser.primaryRole();

        try {
            switch (profileForm.getAction()) {
                case "student-info":
                    userService.updateStudentInfo(loginUser.getUserId(), profileForm);
                    break;
                case "teacher-info":
                    userService.updateTeacherInfo(loginUser.getUserId(), profileForm);
                    break;
                case "society-info":
                    userService.updateSocietyInfo(loginUser.getUserId(), profileForm);
                    break;
                case "basic-info":
                    userService.updateBasicInfo(loginUser.getUserId(), profileForm);
                    break;
                case "password":
                    userService.changePassword(loginUser.getUserId(), profileForm);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException ex) {
            SysUser user = userService.getUser(loginUser.getUserId());
            if ("password".equals(profileForm.getAction())) {
                profileForm.setPasswordError(ex.getMessage());
            } else {
                bindingResult.reject("profile.failed", ex.getMessage());
            }
            model.addAttribute("profileForm", profileForm);
            model.addAttribute("roleKey", roleKey);
            model.addAttribute("userInfo", user);
            return "user/profile";
        }

        redirectAttributes.addFlashAttribute("success", "Profile saved successfully");
        return "redirect:/profile";
    }

    private ProfileForm createProfileForm(SysUser user) {
        ProfileForm form = new ProfileForm();
        form.setNickName(user.getNickName());
        form.setEmail(user.getEmail());
        form.setPhonenumber(user.getPhonenumber());
        form.setSex(user.getSex() != null ? user.getSex() : "2");
        form.setStudentNo(user.getStudentNo());
        form.setEmployeeNo(user.getEmployeeNo());
        form.setTitle(user.getTitle());
        form.setDeptName(user.getDeptName());
        form.setGrade(user.getGrade());
        form.setClassName(user.getClassName());
        return form;
    }
}
