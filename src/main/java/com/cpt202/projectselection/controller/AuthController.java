package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.service.ActivationMailService;
import com.cpt202.projectselection.service.ActivationNotice;
import com.cpt202.projectselection.service.RegisterRequest;
import com.cpt202.projectselection.service.StudentRegisterRequest;
import com.cpt202.projectselection.service.TeacherRegisterRequest;
import com.cpt202.projectselection.service.UserService;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final ActivationMailService activationMailService;

    public AuthController(UserService userService, ActivationMailService activationMailService) {
        this.userService = userService;
        this.activationMailService = activationMailService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String inactive,
                        @RequestParam(required = false) String disabled,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String resend,
                        @RequestParam(required = false) String resendError,
                        @RequestParam(required = false) String showResend,
                        @RequestParam(required = false) String email,
                        Model model) {
        // 添加日志输出
        System.out.println("[AuthController] /login accessed");
        System.out.println("[AuthController] error=" + error);
        System.out.println("[AuthController] inactive=" + inactive);
        System.out.println("[AuthController] disabled=" + disabled);
        System.out.println("[AuthController] showResend=" + showResend);
        System.out.println("[AuthController] email=" + email);
        
        model.addAttribute("hasError", error != null);
        model.addAttribute("hasInactive", inactive != null);
        model.addAttribute("hasDisabled", disabled != null);
        model.addAttribute("hasLogout", logout != null);
        model.addAttribute("hasResend", resend != null);
        model.addAttribute("hasResendError", resendError != null);
        model.addAttribute("showResendForm", showResend != null);
        model.addAttribute("inactiveEmail", email);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerRequest", bindingResult);
            redirectAttributes.addFlashAttribute("registerRequest", registerRequest);
            return "redirect:/register";
        }
        ActivationNotice notice;
        try {
            notice = userService.register(registerRequest);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerRequest", bindingResult);
            redirectAttributes.addFlashAttribute("registerRequest", registerRequest);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
        activationMailService.sendActivation(notice.getEmail(), activationLink(notice));
        redirectAttributes.addAttribute("email", notice.getEmail());
        return "redirect:/activation-pending";
    }

    @GetMapping("/register/student")
    public String studentRegisterForm(Model model) {
        if (!model.containsAttribute("studentRegisterRequest")) {
            model.addAttribute("studentRegisterRequest", new StudentRegisterRequest());
        }
        return "auth/register-student";
    }

    @PostMapping("/register/student")
    public String studentRegister(@Valid @ModelAttribute StudentRegisterRequest studentRegisterRequest,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.studentRegisterRequest", bindingResult);
            redirectAttributes.addFlashAttribute("studentRegisterRequest", studentRegisterRequest);
            return "redirect:/register/student";
        }
        ActivationNotice notice;
        try {
            notice = userService.registerStudent(studentRegisterRequest);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.studentRegisterRequest", bindingResult);
            if (ex.getMessage().contains("Student ID")) {
                bindingResult.rejectValue("studentNo", "studentNo.duplicate", ex.getMessage());
            } else {
                bindingResult.reject("register.failed", ex.getMessage());
            }
            redirectAttributes.addFlashAttribute("studentRegisterRequest", studentRegisterRequest);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register/student";
        }
        activationMailService.sendActivation(notice.getEmail(), activationLink(notice));
        redirectAttributes.addAttribute("email", notice.getEmail());
        return "redirect:/activation-pending";
    }

    @GetMapping("/register/teacher")
    public String teacherRegisterForm(Model model) {
        if (!model.containsAttribute("teacherRegisterRequest")) {
            model.addAttribute("teacherRegisterRequest", new TeacherRegisterRequest());
        }
        return "auth/register-teacher";
    }

    @PostMapping("/register/teacher")
    public String teacherRegister(@Valid @ModelAttribute TeacherRegisterRequest teacherRegisterRequest,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.teacherRegisterRequest", bindingResult);
            redirectAttributes.addFlashAttribute("teacherRegisterRequest", teacherRegisterRequest);
            return "redirect:/register/teacher";
        }
        ActivationNotice notice;
        try {
            notice = userService.registerTeacher(teacherRegisterRequest);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.teacherRegisterRequest", bindingResult);
            redirectAttributes.addFlashAttribute("teacherRegisterRequest", teacherRegisterRequest);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register/teacher";
        }
        activationMailService.sendActivation(notice.getEmail(), activationLink(notice));
        redirectAttributes.addAttribute("email", notice.getEmail());
        return "redirect:/activation-pending";
    }

    @GetMapping("/activation-pending")
    public String activationPending(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "auth/activation-pending";
    }

    @GetMapping("/activate")
    public String activate(@RequestParam(required = false) String token,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            userService.activateAccount(token);
            return "redirect:/activation-success";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/activation-result";
        }
    }

    @GetMapping("/activation-success")
    public String activationSuccess() {
        return "auth/activation-success";
    }

    @GetMapping("/activation/resend")
    public String resendForm() {
        return "auth/resend-activation";
    }

    @GetMapping("/activation-resend")
    public String activationResendForm(@RequestParam(required = false) String email,
                                       @RequestParam(required = false) String resend,
                                       @RequestParam(required = false) String resendError,
                                       Model model) {
        model.addAttribute("email", email);
        model.addAttribute("hasResend", resend != null);
        model.addAttribute("hasResendError", resendError != null);
        return "auth/activation-resend";
    }

    @PostMapping("/activation/resend")
    public String resend(@RequestParam String email,
                         @RequestParam(required = false) String source,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            ActivationNotice notice = userService.resendActivation(email);
            activationMailService.sendActivation(notice.getEmail(), activationLink(notice));
            if ("login".equals(source)) {
                redirectAttributes.addAttribute("inactive", "");
                redirectAttributes.addAttribute("resend", "");
                redirectAttributes.addAttribute("email", notice.getEmail());
                return "redirect:/login";
            }
            if ("activation-resend".equals(source)) {
                redirectAttributes.addAttribute("email", notice.getEmail());
                redirectAttributes.addAttribute("resend", "");
                return "redirect:/activation-resend";
            }
            redirectAttributes.addAttribute("email", notice.getEmail());
            redirectAttributes.addFlashAttribute("message", "A new activation link has been sent to your email.");
            return "redirect:/activation-pending";
        } catch (IllegalArgumentException ex) {
            if ("login".equals(source)) {
                redirectAttributes.addAttribute("inactive", "");
                redirectAttributes.addAttribute("resendError", "");
                redirectAttributes.addAttribute("email", email);
                redirectAttributes.addFlashAttribute("message", ex.getMessage());
                return "redirect:/login";
            }
            if ("activation-resend".equals(source)) {
                redirectAttributes.addAttribute("email", email);
                redirectAttributes.addAttribute("resendError", "");
                redirectAttributes.addFlashAttribute("message", ex.getMessage());
                return "redirect:/activation-resend";
            }
            model.addAttribute("email", email);
            model.addAttribute("error", ex.getMessage());
            return "auth/resend-activation";
        }
    }

    private String activationLink(ActivationNotice notice) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/activate")
                .queryParam("token", notice.getToken())
                .build()
                .toUriString();
    }
}
