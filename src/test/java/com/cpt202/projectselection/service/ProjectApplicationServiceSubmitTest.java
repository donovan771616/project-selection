package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.enums.ApplicationStatus;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectApplicationLogMapper;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationServiceSubmitTest {

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
    void submitApplicationRejectsClosedTopic() {
        com.cpt202.projectselection.domain.ProjectTopic topic =
                createTopic(5L, TopicStatus.Closed.name());
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        assertThatThrownBy(() ->
                applicationService.submitApplication(5L, 20L, "I want this topic", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This topic is not open for applications");
    }

    @Test
    void submitApplicationRejectsDraftTopic() {
        com.cpt202.projectselection.domain.ProjectTopic topic =
                createTopic(5L, TopicStatus.Draft.name());
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        assertThatThrownBy(() ->
                applicationService.submitApplication(5L, 20L, "I want this topic", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This topic is not open for applications");
    }

    @Test
    void submitApplicationRejectsStudentWithPendingApplication() {
        com.cpt202.projectselection.domain.ProjectTopic topic =
                createTopic(5L, TopicStatus.Open.name());
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countPendingByStudentId(20L)).thenReturn(1);

        assertThatThrownBy(() ->
                applicationService.submitApplication(5L, 20L, "I want this topic", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pending application");
    }

    @Test
    void submitApplicationRejectsDuplicateApplication() {
        com.cpt202.projectselection.domain.ProjectTopic topic =
                createTopic(5L, TopicStatus.Open.name());
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countPendingByStudentId(20L)).thenReturn(0);
        when(applicationMapper.countActiveApplicationByTopicAndStudent(5L, 20L)).thenReturn(1);

        assertThatThrownBy(() ->
                applicationService.submitApplication(5L, 20L, "I want this topic", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You already have an active application for this topic");
    }

    @Test
    void submitApplicationSucceedsForOpenTopic() {
        com.cpt202.projectselection.domain.ProjectTopic topic =
                createTopic(5L, TopicStatus.Open.name());
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countPendingByStudentId(20L)).thenReturn(0);
        when(applicationMapper.countActiveApplicationByTopicAndStudent(5L, 20L)).thenReturn(0);

        applicationService.submitApplication(5L, 20L, "I want this topic", "student");

        ArgumentCaptor<ProjectApplication> captor =
                ArgumentCaptor.forClass(ProjectApplication.class);
        verify(applicationMapper).insertApplication(captor.capture());
        ProjectApplication inserted = captor.getValue();
        assertThat(inserted.getTopicId()).isEqualTo(5L);
        assertThat(inserted.getStudentId()).isEqualTo(20L);
        assertThat(inserted.getPersonalNote()).isEqualTo("I want this topic");
        assertThat(inserted.getStatus()).isEqualTo(ApplicationStatus.Pending.name());
        assertThat(inserted.getCreateBy()).isEqualTo("student");
    }

    @Test
    void withdrawApplicationSucceedsForOwnPendingApplication() {
        ProjectApplication app = createApplication(31L, 5L, 20L);
        when(applicationMapper.selectApplicationById(31L)).thenReturn(app);
        when(applicationMapper.withdrawApplication(31L, 20L, "student")).thenReturn(1);

        applicationService.withdrawApplication(31L, 20L, "student");

        verify(applicationMapper).withdrawApplication(31L, 20L, "student");
    }

    @Test
    void withdrawApplicationRejectsOtherStudentsApplication() {
        ProjectApplication app = createApplication(31L, 5L, 20L);
        when(applicationMapper.selectApplicationById(31L)).thenReturn(app);

        assertThatThrownBy(() ->
                applicationService.withdrawApplication(31L, 99L, "otherStudent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can only withdraw your own application");
    }

    @Test
    void withdrawApplicationRejectsNonPendingApplication() {
        ProjectApplication app = createApplication(31L, 5L, 20L);
        app.setStatus(ApplicationStatus.Approved.name());
        when(applicationMapper.selectApplicationById(31L)).thenReturn(app);

        assertThatThrownBy(() ->
                applicationService.withdrawApplication(31L, 20L, "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only pending applications can be processed");
    }

    @Test
    void deleteApplicationSucceedsForExistingApplication() {
        ProjectApplication app = createApplication(31L, 5L, 20L);
        when(applicationMapper.selectApplicationById(31L)).thenReturn(app);

        applicationService.deleteApplication(31L, "admin");

        verify(notificationService).deleteNotificationsForApplication(31L);
        verify(applicationMapper).deleteApplication(31L, "admin");
    }

    @Test
    void deleteApplicationRejectsNonExistentApplication() {
        when(applicationMapper.selectApplicationById(99L)).thenReturn(null);

        assertThatThrownBy(() ->
                applicationService.deleteApplication(99L, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Application does not exist");
    }

    @Test
    void batchRejectProcessesEmptyList() {
        BatchReviewResult result = applicationService.batchRejectApplications(
                Collections.emptyList(), 10L, 10L, "Too many", "teacher");

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0)).contains("Select at least one application");
    }

    @Test
    void batchRejectProcessesMixedSuccessAndFailure() {
        ProjectApplication first = createApplication(31L, 5L, 20L);
        ProjectApplication second = createApplication(32L, 5L, 21L);
        com.cpt202.projectselection.domain.ProjectTopic topic =
                createTopic(5L, TopicStatus.Open.name());

        when(applicationMapper.selectApplicationById(31L)).thenReturn(first);
        when(applicationMapper.selectApplicationById(32L)).thenReturn(second);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        BatchReviewResult result = applicationService.batchRejectApplications(
                Arrays.asList(31L, 32L), 10L, 10L, "Prerequisites not met", "teacher");

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isZero();
        verify(applicationMapper).rejectApplication(eq(31L), eq("Prerequisites not met"), eq(10L), eq("teacher"));
        verify(applicationMapper).rejectApplication(eq(32L), eq("Prerequisites not met"), eq(10L), eq("teacher"));
    }

    @Test
    void topicHasPendingApplicationsDelegatesToMapper() {
        when(applicationMapper.countPendingByTopicId(5L)).thenReturn(3);

        boolean result = applicationService.topicHasPendingApplications(5L);

        assertThat(result).isTrue();
    }

    @Test
    void studentHasAgreedProjectDelegatesToMapper() {
        when(applicationMapper.countAgreedByStudentId(20L)).thenReturn(0);

        boolean result = applicationService.studentHasAgreedProject(20L);

        assertThat(result).isFalse();
    }

    @Test
    void buildReportReturnsAllStatusCounts() {
        when(applicationMapper.countTopicsByStatus(anyString())).thenReturn(0);
        when(applicationMapper.countApplicationsByStatus(anyString())).thenReturn(0);
        when(applicationMapper.countStudentsWithAcceptedTopic()).thenReturn(5);
        when(applicationMapper.countStudentsWithoutAcceptedTopic()).thenReturn(10);

        java.util.Map<String, Integer> report = applicationService.buildReport();

        assertThat(report).containsKey("topicDraft");
        assertThat(report).containsKey("topicOpen");
        assertThat(report).containsKey("applicationPending");
        assertThat(report).containsKey("applicationApproved");
        assertThat(report.get("studentsWithAcceptedTopic")).isEqualTo(5);
        assertThat(report.get("studentsWithoutAcceptedTopic")).isEqualTo(10);
    }

    private com.cpt202.projectselection.domain.ProjectTopic createTopic(Long topicId, String status) {
        com.cpt202.projectselection.domain.ProjectTopic topic =
                new com.cpt202.projectselection.domain.ProjectTopic();
        topic.setTopicId(topicId);
        topic.setTeacherId(10L);
        topic.setStatus(status);
        topic.setMaxStudents(2);
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
