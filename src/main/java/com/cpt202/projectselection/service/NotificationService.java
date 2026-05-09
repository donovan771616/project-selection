package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.Notification;
import com.cpt202.projectselection.domain.NotificationRecipient;
import com.cpt202.projectselection.domain.NotificationSendLog;
import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.enums.NotificationPriority;
import com.cpt202.projectselection.domain.enums.NotificationStatus;
import com.cpt202.projectselection.domain.enums.NotificationType;
import com.cpt202.projectselection.domain.enums.SendStatus;
import com.cpt202.projectselection.mapper.NotificationMapper;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final int CONFIRMATION_DEADLINE_DAYS = 3;

    private final NotificationMapper notificationMapper;
    private final ProjectApplicationMapper applicationMapper;
    private final ProjectTopicMapper topicMapper;

    public NotificationService(NotificationMapper notificationMapper,
                              ProjectApplicationMapper applicationMapper,
                              ProjectTopicMapper topicMapper) {
        this.notificationMapper = notificationMapper;
        this.applicationMapper = applicationMapper;
        this.topicMapper = topicMapper;
    }

    @Transactional
    public void sendApplicationApprovedNotification(Long applicationId, Long operatorId, String operatorName) {
        ProjectApplication application = applicationMapper.selectApplicationById(applicationId);
        if (application == null) {
            return;
        }

        ProjectTopic topic = topicMapper.selectTopicById(application.getTopicId());
        if (topic == null) {
            return;
        }

        String deadline = LocalDateTime.now().plusDays(CONFIRMATION_DEADLINE_DAYS)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Notification notification = new Notification();
        notification.setTitle("Application Approved");
        notification.setContent(String.format(
                "Congratulations! Your application has been approved.\n\n" +
                "Topic: %s\n" +
                "Supervisor: %s\n\n" +
                "Please confirm and sign by %s.",
                topic.getTitle(),
                application.getTeacherName(),
                deadline.replace("00:00", "23:59")
        ));
        notification.setType(NotificationType.APPLICATION_APPROVED.name());
        notification.setRelatedId(applicationId);
        notification.setRelatedType("APPLICATION");
        notification.setSenderId(operatorId);
        notification.setSenderName(operatorName);
        notification.setPriority(NotificationPriority.HIGH.name());
        notification.setStatus(NotificationStatus.PENDING.name());
        notification.setRequireConfirmation(true);
        notification.setConfirmationDeadline(
                LocalDateTime.now().plusDays(CONFIRMATION_DEADLINE_DAYS));
        notification.setSendStatus(SendStatus.SENT.name());
        notification.setCreateBy(operatorName);

        notificationMapper.insertNotification(notification);

        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotificationId(notification.getNotificationId());
        recipient.setUserId(application.getStudentId());
        recipient.setUserName(application.getStudentName());
        notificationMapper.insertRecipient(recipient);

        writeSendLog(notification.getNotificationId(), recipient.getRecipientId(),
                application.getStudentId(), SendStatus.SENT.name(), null);
    }

    @Transactional
    public void sendApplicationRejectedNotification(Long applicationId, Long operatorId,
                                                   String operatorName, String reason) {
        ProjectApplication application = applicationMapper.selectApplicationById(applicationId);
        if (application == null) {
            return;
        }

        ProjectTopic topic = topicMapper.selectTopicById(application.getTopicId());

        Notification notification = new Notification();
        notification.setTitle("Application Rejected");
        notification.setContent(String.format(
                "Unfortunately, your application has been rejected.\n\n" +
                "Topic: %s\n" +
                "Reason: %s\n\n" +
                "Please contact your supervisor if you have any questions.",
                topic != null ? topic.getTitle() : "Unknown Topic",
                reason
        ));
        notification.setType(NotificationType.APPLICATION_REJECTED.name());
        notification.setRelatedId(applicationId);
        notification.setRelatedType("APPLICATION");
        notification.setSenderId(operatorId);
        notification.setSenderName(operatorName);
        notification.setPriority(NotificationPriority.NORMAL.name());
        notification.setStatus(NotificationStatus.PENDING.name());
        notification.setRequireConfirmation(false);
        notification.setSendStatus(SendStatus.SENT.name());
        notification.setCreateBy(operatorName);

        notificationMapper.insertNotification(notification);

        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotificationId(notification.getNotificationId());
        recipient.setUserId(application.getStudentId());
        recipient.setUserName(application.getStudentName());
        notificationMapper.insertRecipient(recipient);

        writeSendLog(notification.getNotificationId(), recipient.getRecipientId(),
                application.getStudentId(), SendStatus.SENT.name(), null);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationMapper.selectUnreadNotifications(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadNotifications(userId);
    }

    public PageResult<Notification> getNotificationsPaged(Long userId, String type,
                                                        String status, String keyword,
                                                        Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        List<Notification> list = notificationMapper.selectNotificationsPaged(
                userId, type, status, keyword,
                pageRequest.getSize(), pageRequest.getOffset());
        int total = notificationMapper.countNotificationsByUserId(userId, type, status, keyword);
        return new PageResult<>(list, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationMapper.updateRecipientReadByUserAndNotification(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void confirmNotification(Long notificationId, Long userId) {
        notificationMapper.updateRecipientConfirmedByUserAndNotification(notificationId, userId);

        Notification notification = notificationMapper.selectNotificationById(notificationId);
        if (notification != null && notification.getRequireConfirmation()) {
            int confirmedCount = notificationMapper.selectRecipientsByNotificationId(notificationId)
                    .stream().filter(NotificationRecipient::getIsConfirmed).collect(java.util.stream.Collectors.toList()).size();
            int totalCount = notificationMapper.selectRecipientsByNotificationId(notificationId).size();

            if (confirmedCount >= totalCount) {
                notificationMapper.updateNotificationStatus(notificationId, NotificationStatus.SIGNED.name());
            }
        }
    }

    public Notification getNotification(Long notificationId) {
        return notificationMapper.selectNotificationById(notificationId);
    }

    public List<NotificationRecipient> getRecipients(Long notificationId) {
        return notificationMapper.selectRecipientsByNotificationId(notificationId);
    }

    @Transactional
    public void deleteNotificationForUser(Long notificationId, Long userId) {
        notificationMapper.deleteRecipient(notificationId, userId);
    }

    @Transactional
    public void deleteNotificationsForApplication(Long applicationId) {
        notificationMapper.deleteNotificationRecipientsByRelatedId(applicationId, "APPLICATION");
        notificationMapper.deleteNotificationsByRelatedId(applicationId, "APPLICATION");
    }

    @Transactional
    public void processOverdueConfirmations() {
        String deadline = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<NotificationRecipient> overdueRecipients =
                notificationMapper.selectUnconfirmedRecipientsByDeadline(deadline);

        for (NotificationRecipient recipient : overdueRecipients) {
            sendOverdueReminderNotification(recipient);
        }
    }

    private void sendOverdueReminderNotification(NotificationRecipient recipient) {
        Notification original = notificationMapper.selectNotificationById(recipient.getNotificationId());
        if (original == null) {
            return;
        }

        Notification reminder = new Notification();
        reminder.setTitle("Confirmation Overdue");
        reminder.setContent(String.format(
                "You have a pending confirmation that is now overdue.\n\n" +
                "Notification: %s\n" +
                "Deadline: %s\n\n" +
                "Please confirm as soon as possible.",
                original.getTitle(),
                original.getConfirmationDeadline() != null ?
                        original.getConfirmationDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Unknown"
        ));
        reminder.setType(NotificationType.OVERDUE_REMINDER.name());
        reminder.setRelatedId(original.getRelatedId());
        reminder.setRelatedType(original.getRelatedType());
        reminder.setSenderId(0L);
        reminder.setSenderName("System");
        reminder.setPriority(NotificationPriority.HIGH.name());
        reminder.setStatus(NotificationStatus.PENDING.name());
        reminder.setRequireConfirmation(false);
        reminder.setSendStatus(SendStatus.SENT.name());
        reminder.setCreateBy("system");

        notificationMapper.insertNotification(reminder);

        NotificationRecipient newRecipient = new NotificationRecipient();
        newRecipient.setNotificationId(reminder.getNotificationId());
        newRecipient.setUserId(recipient.getUserId());
        newRecipient.setUserName(recipient.getUserName());
        notificationMapper.insertRecipient(newRecipient);
    }

    @Transactional
    public void sendPushFailedNotification(Long notificationId, String errorMessage) {
        Notification original = notificationMapper.selectNotificationById(notificationId);
        if (original == null) {
            return;
        }

        Notification failed = new Notification();
        failed.setTitle("Notification Delivery Failed");
        failed.setContent(String.format(
                "A notification failed to be delivered.\n\n" +
                "Notification: %s\n" +
                "Error: %s",
                original.getTitle(),
                errorMessage
        ));
        failed.setType(NotificationType.PUSH_FAILED.name());
        failed.setRelatedId(notificationId);
        failed.setRelatedType("NOTIFICATION");
        failed.setSenderId(0L);
        failed.setSenderName("System");
        failed.setPriority(NotificationPriority.HIGH.name());
        failed.setStatus(NotificationStatus.PENDING.name());
        failed.setRequireConfirmation(false);
        failed.setSendStatus(SendStatus.FAILED.name());
        failed.setSendError(errorMessage);
        failed.setCreateBy("system");

        notificationMapper.insertNotification(failed);

        List<NotificationRecipient> originalRecipients =
                notificationMapper.selectRecipientsByNotificationId(notificationId);

        for (NotificationRecipient originalRecipient : originalRecipients) {
            NotificationRecipient adminRecipient = new NotificationRecipient();
            adminRecipient.setNotificationId(failed.getNotificationId());
            adminRecipient.setUserId(originalRecipient.getUserId());
            adminRecipient.setUserName(originalRecipient.getUserName());
            notificationMapper.insertRecipient(adminRecipient);
        }
    }

    private void writeSendLog(Long notificationId, Long recipientId, Long userId,
                              String status, String errorMessage) {
        NotificationSendLog log = new NotificationSendLog();
        log.setNotificationId(notificationId);
        log.setRecipientId(recipientId);
        log.setUserId(userId);
        log.setSendStatus(status);
        log.setErrorMessage(errorMessage);
        log.setRetryCount(0);
        notificationMapper.insertSendLog(log);
    }

    public List<NotificationSendLog> getSendLogs(Long notificationId) {
        return notificationMapper.selectSendLogsByNotificationId(notificationId);
    }
}
