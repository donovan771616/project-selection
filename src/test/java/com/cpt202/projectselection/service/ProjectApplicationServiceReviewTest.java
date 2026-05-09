package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.ProjectApplicationLog;
import com.cpt202.projectselection.domain.enums.ApplicationStatus;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectApplicationLogMapper;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationServiceReviewTest {

    @Mock
    private ProjectApplicationMapper applicationMapper;

    @Mock
    private ProjectTopicMapper topicMapper;

    @Mock
    private ProjectApplicationLogMapper logMapper;

    @Mock
    private NotificationService notificationService;

    private ProjectApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ProjectApplicationService(
                applicationMapper, topicMapper, logMapper, notificationService);
    }

    @Test
    void acceptApplicationRejectsAlreadyFullTopic() {
        ProjectApplication application = createApplication(31L, 5L, 20L);
        com.cpt202.projectselection.domain.ProjectTopic topic = createTopic(5L, 10L, TopicStatus.Open.name(), 1);

        when(applicationMapper.selectApplicationById(31L)).thenReturn(application);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countAcceptedByTopicId(5L)).thenReturn(1);

        assertThatThrownBy(() ->
                applicationService.acceptApplication(31L, 10L, 10L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This topic is already full");
    }

    @Test
    void acceptApplicationRejectsNonExistentApplication() {
        when(applicationMapper.selectApplicationById(99L)).thenReturn(null);

        assertThatThrownBy(() ->
                applicationService.acceptApplication(99L, 10L, 10L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Application does not exist");
    }

    @Test
    void acceptApplicationRejectsNonPendingApplication() {
        ProjectApplication application = createApplication(31L, 5L, 20L);
        application.setStatus(ApplicationStatus.Rejected.name());
        when(applicationMapper.selectApplicationById(31L)).thenReturn(application);

        assertThatThrownBy(() ->
                applicationService.acceptApplication(31L, 10L, 10L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only pending applications can be processed");
    }

    @Test
    void acceptApplicationRejectsWrongTeacher() {
        ProjectApplication application = createApplication(31L, 5L, 20L);
        com.cpt202.projectselection.domain.ProjectTopic topic = createTopic(5L, 10L, TopicStatus.Open.name(), 2);

        when(applicationMapper.selectApplicationById(31L)).thenReturn(application);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        assertThatThrownBy(() ->
                applicationService.acceptApplication(31L, 99L, 99L, "otherTeacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can only review applications for your own topics");
    }

    @Test
    void acceptApplicationSucceedsAndAutoRejectsOthers() {
        ProjectApplication application = createApplication(31L, 5L, 20L);
        com.cpt202.projectselection.domain.ProjectTopic topic = createTopic(5L, 10L, TopicStatus.Open.name(), 2);

        when(applicationMapper.selectApplicationById(31L)).thenReturn(application);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countAcceptedByTopicId(5L)).thenReturn(0);

        applicationService.acceptApplication(31L, 10L, 10L, "teacher");

        verify(applicationMapper).acceptApplication(31L, "teacher");
        verify(applicationMapper).rejectOtherPendingApplications(
                eq(20L), eq(31L),
                eq("Automatically rejected because another topic has been accepted"),
                eq(10L), eq("teacher"));

        ArgumentCaptor<ProjectApplicationLog> logCaptor =
                ArgumentCaptor.forClass(ProjectApplicationLog.class);
        verify(logMapper).insertLog(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo(ApplicationStatus.Approved.name());
    }

    @Test
    void rejectApplicationSucceedsForValidApplication() {
        ProjectApplication application = createApplication(31L, 5L, 20L);
        com.cpt202.projectselection.domain.ProjectTopic topic = createTopic(5L, 10L, TopicStatus.Open.name(), 2);

        when(applicationMapper.selectApplicationById(31L)).thenReturn(application);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        applicationService.rejectApplication(31L, 10L, 10L, "Prerequisites not met", "teacher");

        verify(applicationMapper).rejectApplication(eq(31L), eq("Prerequisites not met"), eq(10L), eq("teacher"));

        ArgumentCaptor<ProjectApplicationLog> logCaptor =
                ArgumentCaptor.forClass(ProjectApplicationLog.class);
        verify(logMapper).insertLog(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo(ApplicationStatus.Rejected.name());
        assertThat(logCaptor.getValue().getReason()).isEqualTo("Prerequisites not met");
    }

    @Test
    void batchAcceptProcessesEmptyList() {
        BatchReviewResult result = applicationService.batchAcceptApplications(
                java.util.Collections.emptyList(), 10L, 10L, "teacher");

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getFailureCount()).isEqualTo(1);
    }

    @Test
    void batchAcceptProcessesWithMixedResults() {
        ProjectApplication first = createApplication(31L, 5L, 20L);
        ProjectApplication second = createApplication(32L, 5L, 21L);
        com.cpt202.projectselection.domain.ProjectTopic topic = createTopic(5L, 10L, TopicStatus.Open.name(), 2);

        when(applicationMapper.selectApplicationById(31L)).thenReturn(first);
        when(applicationMapper.selectApplicationById(32L)).thenReturn(second);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countAcceptedByTopicId(5L)).thenReturn(0);

        BatchReviewResult result = applicationService.batchAcceptApplications(
                java.util.Arrays.asList(31L, 32L), 10L, 10L, "teacher");

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isZero();
    }

    @Test
    void rejectConflictingPendingApplicationsCallsMapper() {
        when(applicationMapper.rejectOtherPendingApplications(
                eq(20L), eq(99L),
                eq("Automatically rejected because another topic has been accepted"),
                any(), eq("system"))).thenReturn(2);

        int count = applicationService.rejectConflictingPendingApplications(20L, 99L);

        assertThat(count).isEqualTo(2);
    }

    private com.cpt202.projectselection.domain.ProjectTopic createTopic(
            Long topicId, Long teacherId, String status, int maxStudents) {
        com.cpt202.projectselection.domain.ProjectTopic topic =
                new com.cpt202.projectselection.domain.ProjectTopic();
        topic.setTopicId(topicId);
        topic.setTeacherId(teacherId);
        topic.setStatus(status);
        topic.setMaxStudents(maxStudents);
        return topic;
    }

    private ProjectApplication createApplication(Long applicationId, Long topicId, Long studentId) {
        ProjectApplication application = new ProjectApplication();
        application.setApplicationId(applicationId);
        application.setTopicId(topicId);
        application.setStudentId(studentId);
        application.setStatus(ApplicationStatus.Pending.name());
        return application;
    }
}
