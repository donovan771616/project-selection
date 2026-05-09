package com.cpt202.projectselection.controller;

import com.cpt202.projectselection.common.CurrentUser;
import com.cpt202.projectselection.domain.Notification;
import com.cpt202.projectselection.domain.NotificationRecipient;
import com.cpt202.projectselection.domain.NotificationSendLog;
import com.cpt202.projectselection.security.LoginUser;
import com.cpt202.projectselection.service.NotificationService;
import com.cpt202.projectselection.service.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public String list(@RequestParam(required = false) String type,
                      @RequestParam(required = false) String status,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) Integer page,
                      Model model) {
        LoginUser loginUser = CurrentUser.get();
        PageResult<Notification> pageResult = notificationService.getNotificationsPaged(
                loginUser.getUserId(), type, status, keyword, page, 10);
        int unreadCount = notificationService.getUnreadCount(loginUser.getUserId());

        model.addAttribute("notificationsPage", pageResult);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        return "notifications";
    }

    @GetMapping("/notifications/unread")
    @PreAuthorize("isAuthenticated()")
    public String getUnreadNotifications(Model model) {
        LoginUser loginUser = CurrentUser.get();
        List<Notification> unread = notificationService.getUnreadNotifications(loginUser.getUserId());
        model.addAttribute("unreadNotifications", unread);
        return "notifications :: unread-list";
    }

    @GetMapping("/notifications/popup")
    @PreAuthorize("isAuthenticated()")
    public String getUnreadPopup(Model model) {
        LoginUser loginUser = CurrentUser.get();
        List<Notification> unread = notificationService.getUnreadNotifications(loginUser.getUserId());
        model.addAttribute("popupNotifications", unread);
        return "notifications :: popup-content";
    }

    @GetMapping("/notifications/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public String detail(@PathVariable Long notificationId, Model model) {
        LoginUser loginUser = CurrentUser.get();
        Notification notification = notificationService.getNotification(notificationId);
        if (notification == null) {
            return "redirect:/notifications";
        }

        notificationService.markAsRead(notificationId, loginUser.getUserId());

        List<NotificationRecipient> recipients = notificationService.getRecipients(notificationId);
        List<NotificationSendLog> sendLogs = notificationService.getSendLogs(notificationId);

        model.addAttribute("notification", notification);
        model.addAttribute("recipients", recipients);
        model.addAttribute("sendLogs", sendLogs);
        model.addAttribute("isOwner", recipients.stream()
                .anyMatch(r -> r.getUserId().equals(loginUser.getUserId())));
        return "notification-detail";
    }

    @PostMapping("/notifications/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public String markAsRead(@PathVariable Long notificationId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        notificationService.markAsRead(notificationId, loginUser.getUserId());
        return "redirect:/notifications/" + notificationId;
    }

    @PostMapping("/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public String markAllAsRead(RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        notificationService.markAllAsRead(loginUser.getUserId());
        redirectAttributes.addFlashAttribute("success", "All notifications marked as read");
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{notificationId}/confirm")
    @PreAuthorize("isAuthenticated()")
    public String confirm(@PathVariable Long notificationId, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = CurrentUser.get();
        notificationService.markAsRead(notificationId, loginUser.getUserId());
        notificationService.confirmNotification(notificationId, loginUser.getUserId());
        redirectAttributes.addFlashAttribute("success", "Notification confirmed");
        return "redirect:/notifications/" + notificationId;
    }

    @GetMapping("/notifications/count")
    @ResponseBody
    public Map<String, Object> getUnreadCount() {
        LoginUser loginUser = CurrentUser.get();
        int count = 0;
        if (loginUser != null) {
            count = notificationService.getUnreadCount(loginUser.getUserId());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    @GetMapping("/api/notifications/unread")
    @ResponseBody
    public Map<String, Object> getUnreadNotificationsApi() {
        LoginUser loginUser = CurrentUser.get();
        List<Notification> unread = notificationService.getUnreadNotifications(loginUser.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("count", unread.size());
        result.put("notifications", unread);
        return result;
    }

    @PostMapping("/notifications/{notificationId}/delete")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public Map<String, Object> deleteNotification(@PathVariable Long notificationId) {
        LoginUser loginUser = CurrentUser.get();
        notificationService.deleteNotificationForUser(notificationId, loginUser.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
