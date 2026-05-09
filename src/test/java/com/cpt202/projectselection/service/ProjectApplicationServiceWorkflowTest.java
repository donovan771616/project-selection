package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.enums.ApplicationStatus;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectApplicationLogMapper;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import com.cpt202.projectselection.service.NotificationService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationServiceWorkflowTest {

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
        applicationService = new ProjectApplicationService(applicationMapper, topicMapper, logMapper, notificationService);
    }

    @Test
    void submitApplicationRejectsStudentWhoAlreadyHasAcceptedTopic() {
        ProjectTopic topic = topic(TopicStatus.Open.name());
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countPendingByStudentId(20L)).thenReturn(1);

        assertThatThrownBy(() -> applicationService.submitApplication(5L, 20L, "I can contribute", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pending application");
    }

    @Test
    void acceptApplicationWritesReviewLogAndAutoRejectMetadata() {
        ProjectApplication application = application(31L, 5L, 20L);
        ProjectTopic topic = topic(TopicStatus.Open.name());
        when(applicationMapper.selectApplicationById(31L)).thenReturn(application);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        applicationService.acceptApplication(31L, 10L, 10L, "teacher");

        verify(applicationMapper).acceptApplication(31L, "teacher");
        verify(applicationMapper).rejectOtherPendingApplications(20L, 31L,
                "Automatically rejected because another topic has been accepted", 10L, "teacher");
        verify(logMapper).insertLog(argThatLog(31L, ApplicationStatus.Approved.name()));
    }

    @Test
    void batchRejectProcessesAllSelectedApplications() {
        ProjectApplication first = application(31L, 5L, 20L);
        ProjectApplication second = application(32L, 5L, 21L);
        ProjectTopic topic = topic(TopicStatus.Open.name());
        when(applicationMapper.selectApplicationById(31L)).thenReturn(first);
        when(applicationMapper.selectApplicationById(32L)).thenReturn(second);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        BatchReviewResult result = applicationService.batchRejectApplications(
                Arrays.asList(31L, 32L), 10L, 10L, "Prerequisites are not aligned", "teacher");

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isZero();
        verify(applicationMapper).rejectApplication(31L, "Prerequisites are not aligned", 10L, "teacher");
        verify(applicationMapper).rejectApplication(32L, "Prerequisites are not aligned", 10L, "teacher");
    }

    private ProjectApplication application(Long applicationId, Long topicId, Long studentId) {
        ProjectApplication application = new ProjectApplication();
        application.setApplicationId(applicationId);
        application.setTopicId(topicId);
        application.setStudentId(studentId);
        application.setStatus(ApplicationStatus.Pending.name());
        return application;
    }

    private ProjectTopic topic(String status) {
        ProjectTopic topic = new ProjectTopic();
        topic.setTopicId(5L);
        topic.setTeacherId(10L);
        topic.setStatus(status);
        topic.setMaxStudents(2);
        return topic;
    }

    private com.cpt202.projectselection.domain.ProjectApplicationLog argThatLog(Long applicationId, String action) {
        return org.mockito.ArgumentMatchers.argThat(log ->
                applicationId.equals(log.getApplicationId()) && action.equals(log.getAction()));
    }
}
