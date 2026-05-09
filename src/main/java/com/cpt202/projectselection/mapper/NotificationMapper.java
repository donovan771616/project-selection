package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.Notification;
import com.cpt202.projectselection.domain.NotificationRecipient;
import com.cpt202.projectselection.domain.NotificationSendLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotificationMapper {

    int insertNotification(Notification notification);

    int updateNotification(Notification notification);

    Notification selectNotificationById(@Param("notificationId") Long notificationId);

    List<Notification> selectNotificationsByUserId(@Param("userId") Long userId);

    List<Notification> selectNotificationsPaged(@Param("userId") Long userId,
                                               @Param("type") String type,
                                               @Param("status") String status,
                                               @Param("keyword") String keyword,
                                               @Param("limit") int limit,
                                               @Param("offset") int offset);

    int countNotificationsByUserId(@Param("userId") Long userId,
                                  @Param("type") String type,
                                  @Param("status") String status,
                                  @Param("keyword") String keyword);

    List<Notification> selectUnreadNotifications(@Param("userId") Long userId);

    int countUnreadNotifications(@Param("userId") Long userId);

    int insertRecipient(NotificationRecipient recipient);

    int insertRecipientsBatch(@Param("list") List<NotificationRecipient> recipients);

    int updateRecipientRead(@Param("recipientId") Long recipientId);

    int updateRecipientReadByUserAndNotification(@Param("notificationId") Long notificationId,
                                                 @Param("userId") Long userId);

    int markAllAsReadByUserId(@Param("userId") Long userId);

    int updateRecipientConfirmed(@Param("recipientId") Long recipientId);

    int updateRecipientConfirmedByUserAndNotification(@Param("notificationId") Long notificationId,
                                                    @Param("userId") Long userId);

    List<NotificationRecipient> selectRecipientsByNotificationId(@Param("notificationId") Long notificationId);

    List<NotificationRecipient> selectUnconfirmedRecipientsByDeadline(@Param("deadline") String deadline);

    List<NotificationRecipient> selectRecipientsNeedingReminder(@Param("userId") Long userId);

    int insertSendLog(NotificationSendLog sendLog);

    int updateSendLogStatus(@Param("logId") Long logId,
                           @Param("sendStatus") String sendStatus,
                           @Param("errorMessage") String errorMessage);

    List<NotificationSendLog> selectSendLogsByNotificationId(@Param("notificationId") Long notificationId);

    List<Notification> selectNotificationsNeedingConfirmationReminder();

    int updateNotificationStatus(@Param("notificationId") Long notificationId,
                                @Param("status") String status);

    int deleteRecipient(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    int deleteNotificationsByRelatedId(@Param("relatedId") Long relatedId, @Param("relatedType") String relatedType);

    int deleteNotificationRecipientsByRelatedId(@Param("relatedId") Long relatedId, @Param("relatedType") String relatedType);
}
