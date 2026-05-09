package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.ProjectApplicationLog;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.enums.ApplicationStatus;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectApplicationLogMapper;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectApplicationService {

    private static final String AUTO_REJECT_REASON = "Automatically rejected because another topic has been accepted";

    private final ProjectApplicationMapper applicationMapper;
    private final ProjectTopicMapper topicMapper;
    private final ProjectApplicationLogMapper logMapper;
    private final NotificationService notificationService;

    public ProjectApplicationService(ProjectApplicationMapper applicationMapper,
                                     ProjectTopicMapper topicMapper,
                                     ProjectApplicationLogMapper logMapper,
                                     @Lazy NotificationService notificationService) {
        this.applicationMapper = applicationMapper;
        this.topicMapper = topicMapper;
        this.logMapper = logMapper;
        this.notificationService = notificationService;
    }

    public ProjectApplication getApplication(Long applicationId) {
        return applicationMapper.selectApplicationById(applicationId);
    }

    public List<ProjectApplicationLog> getApplicationLogs(Long applicationId) {
        return logMapper.selectLogsByApplicationId(applicationId);
    }

    public List<ProjectApplication> listTeacherPending(Long teacherId) {
        return applicationMapper.selectPendingByTeacherId(teacherId);
    }

    public List<ProjectApplication> listTeacherHistory(Long teacherId) {
        return applicationMapper.selectHistoryByTeacherId(teacherId);
    }

    public List<ProjectApplication> listStudentHistory(Long studentId) {
        return applicationMapper.selectHistoryByStudentId(studentId);
    }

    public List<ProjectApplication> listAllApplications(String status, String keyword) {
        return applicationMapper.selectAllApplications(status, keyword);
    }

    public PageResult<ProjectApplication> listTeacherPendingPage(Long teacherId, String keyword, String sort,
                                                                 Long topicId, Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return new PageResult<>(
                applicationMapper.selectPendingByTeacherIdPaged(teacherId, keyword, normalizeSort(sort), topicId,
                        pageRequest.getSize(), pageRequest.getOffset()),
                pageRequest.getPage(),
                pageRequest.getSize(),
                applicationMapper.countPendingByTeacherIdFiltered(teacherId, keyword, topicId)
        );
    }

    public PageResult<ProjectApplication> listTeacherHistoryPage(Long teacherId, String status, String keyword,
                                                                 String sort, Long topicId, Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return new PageResult<>(
                applicationMapper.selectHistoryByTeacherIdPaged(teacherId, status, keyword, normalizeSort(sort), topicId,
                        pageRequest.getSize(), pageRequest.getOffset()),
                pageRequest.getPage(),
                pageRequest.getSize(),
                applicationMapper.countHistoryByTeacherIdFiltered(teacherId, status, keyword, topicId)
        );
    }

    public PageResult<ProjectApplication> listStudentHistoryPage(Long studentId, String status, String keyword,
                                                                 String sort, Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return new PageResult<>(
                applicationMapper.selectHistoryByStudentIdPaged(studentId, status, keyword, normalizeSort(sort),
                        pageRequest.getSize(), pageRequest.getOffset()),
                pageRequest.getPage(),
                pageRequest.getSize(),
                applicationMapper.countHistoryByStudentIdFiltered(studentId, status, keyword)
        );
    }

    public PageResult<ProjectApplication> listAllApplicationsPage(String status, String keyword, String sort,
                                                                  Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return new PageResult<>(
                applicationMapper.selectAllApplicationsPaged(status, keyword, normalizeSort(sort),
                        pageRequest.getSize(), pageRequest.getOffset()),
                pageRequest.getPage(),
                pageRequest.getSize(),
                applicationMapper.countAllApplicationsFiltered(status, keyword)
        );
    }

    @Transactional
    public void submitApplication(Long topicId, Long studentId, String note, String operator) {
        ProjectTopic topic = topicMapper.selectTopicById(topicId);
        if (topic == null || !TopicStatus.Open.name().equals(topic.getStatus())) {
            throw new IllegalArgumentException("This topic is not open for applications");
        }
        if (applicationMapper.countPendingByStudentId(studentId) > 0) {
            throw new IllegalArgumentException("You already have a pending application. Please wait for it to be reviewed or withdraw it before submitting a new one.");
        }
        if (applicationMapper.countActiveApplicationByTopicAndStudent(topicId, studentId) > 0) {
            throw new IllegalArgumentException("You already have an active application for this topic");
        }
        ProjectApplication application = new ProjectApplication();
        application.setTopicId(topicId);
        application.setStudentId(studentId);
        application.setPersonalNote(note);
        application.setStatus(ApplicationStatus.Pending.name());
        application.setCreateBy(operator);
        applicationMapper.insertApplication(application);
        writeLog(application.getApplicationId(), "Submitted", studentId, operator, "Application submitted");
    }

    @Transactional
    public void acceptApplication(Long applicationId, Long teacherId, String operator) {
        acceptApplication(applicationId, teacherId, null, operator);
    }

    @Transactional
    public void acceptApplication(Long applicationId, Long teacherId, Long operatorId, String operator) {
        ProjectApplication application = requireApplication(applicationId);
        ProjectTopic topic = topicMapper.selectTopicById(application.getTopicId());
        ensureTeacherOwnsTopic(topic, teacherId);
        if (applicationMapper.countAcceptedByTopicId(topic.getTopicId()) >= topic.getMaxStudents()) {
            throw new IllegalArgumentException("This topic is already full");
        }
        applicationMapper.acceptApplication(applicationId, operator);
        applicationMapper.rejectOtherPendingApplications(application.getStudentId(), applicationId,
                AUTO_REJECT_REASON, operatorId, operator);
        writeLog(applicationId, ApplicationStatus.Approved.name(), operatorId, operator, "Application accepted");
    }

    @Transactional
    public void rejectApplication(Long applicationId, Long teacherId, Long rejectBy, String reason, String operator) {
        ProjectApplication application = requireApplication(applicationId);
        ProjectTopic topic = topicMapper.selectTopicById(application.getTopicId());
        ensureTeacherOwnsTopic(topic, teacherId);
        applicationMapper.rejectApplication(applicationId, reason, rejectBy, operator);
        writeLog(applicationId, ApplicationStatus.Rejected.name(), rejectBy, operator, reason);
    }

    @Transactional
    public void withdrawApplication(Long applicationId, Long studentId, String operator) {
        ProjectApplication application = requireApplication(applicationId);
        if (!studentId.equals(application.getStudentId())) {
            throw new IllegalArgumentException("You can only withdraw your own application");
        }
        int updated = applicationMapper.withdrawApplication(applicationId, studentId, operator);
        if (updated == 0) {
            throw new IllegalArgumentException("Only pending applications can be withdrawn");
        }
        writeLog(applicationId, ApplicationStatus.Withdrawn.name(), studentId, operator, "Application withdrawn");
    }

    @Transactional
    public BatchReviewResult batchAcceptApplications(List<Long> applicationIds, Long teacherId,
                                                     Long operatorId, String operator) {
        BatchReviewResult result = new BatchReviewResult();
        if (applicationIds == null || applicationIds.isEmpty()) {
            result.addFailure(0L, "Select at least one application");
            return result;
        }
        for (Long applicationId : applicationIds) {
            try {
                acceptApplication(applicationId, teacherId, operatorId, operator);
                result.addSuccess(applicationId);
            } catch (IllegalArgumentException ex) {
                result.addFailure(applicationId, ex.getMessage());
            }
        }
        return result;
    }

    @Transactional
    public BatchReviewResult batchRejectApplications(List<Long> applicationIds, Long teacherId,
                                                     Long rejectBy, String reason, String operator) {
        BatchReviewResult result = new BatchReviewResult();
        if (applicationIds == null || applicationIds.isEmpty()) {
            result.addFailure(0L, "Select at least one application");
            return result;
        }
        for (Long applicationId : applicationIds) {
            try {
                rejectApplication(applicationId, teacherId, rejectBy, reason, operator);
                result.addSuccess(applicationId);
            } catch (IllegalArgumentException ex) {
                result.addFailure(applicationId, ex.getMessage());
            }
        }
        return result;
    }

    public Map<String, Integer> buildReport() {
        Map<String, Integer> report = new HashMap<>();
        for (TopicStatus status : TopicStatus.values()) {
            report.put("topic" + status.name(), applicationMapper.countTopicsByStatus(status.name()));
        }
        for (ApplicationStatus status : ApplicationStatus.values()) {
            report.put("application" + status.name(), applicationMapper.countApplicationsByStatus(status.name()));
        }
        report.put("studentsWithAcceptedTopic", applicationMapper.countStudentsWithAcceptedTopic());
        report.put("studentsWithoutAcceptedTopic", applicationMapper.countStudentsWithoutAcceptedTopic());
        return report;
    }

    public boolean studentHasAgreedProject(Long studentId) {
        return applicationMapper.countAgreedByStudentId(studentId) > 0;
    }

    public boolean studentHasPendingApplication(Long studentId) {
        return applicationMapper.countPendingByStudentId(studentId) > 0;
    }

    public int rejectConflictingPendingApplications(Long studentId, Long acceptedApplicationId) {
        return applicationMapper.rejectOtherPendingApplications(studentId, acceptedApplicationId,
                AUTO_REJECT_REASON, null, "system");
    }

    @Transactional
    public void deleteApplication(Long applicationId, String operator) {
        ProjectApplication application = applicationMapper.selectApplicationById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application does not exist");
        }
        notificationService.deleteNotificationsForApplication(applicationId);
        applicationMapper.deleteApplication(applicationId, operator);
    }

    public boolean topicHasPendingApplications(Long topicId) {
        return applicationMapper.countPendingByTopicId(topicId) > 0;
    }

    private ProjectApplication requireApplication(Long applicationId) {
        ProjectApplication application = applicationMapper.selectApplicationById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application does not exist");
        }
        if (!ApplicationStatus.Pending.name().equals(application.getStatus())) {
            throw new IllegalArgumentException("Only pending applications can be processed");
        }
        return application;
    }

    private void ensureTeacherOwnsTopic(ProjectTopic topic, Long teacherId) {
        if (topic == null || (!teacherId.equals(topic.getTeacherId()) && teacherId > 0)) {
            throw new IllegalArgumentException("You can only review applications for your own topics");
        }
    }

    private void writeLog(Long applicationId, String action, Long operatorId, String operatorName, String reason) {
        if (applicationId == null) {
            return;
        }
        ProjectApplicationLog log = new ProjectApplicationLog();
        log.setApplicationId(applicationId);
        log.setAction(action);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setReason(reason);
        logMapper.insertLog(log);
    }

    private String normalizeSort(String sort) {
        if ("oldest".equals(sort) || "student".equals(sort) || "topic".equals(sort) || "status".equals(sort)) {
            return sort;
        }
        return "newest";
    }
}
