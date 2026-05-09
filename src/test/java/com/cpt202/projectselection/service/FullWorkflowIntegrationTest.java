package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.ProjectApplicationLog;
import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.SysUser;
import com.cpt202.projectselection.domain.enums.ApplicationStatus;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectApplicationLogMapper;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
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
class FullWorkflowIntegrationTest {

    @Mock
    private ProjectApplicationMapper applicationMapper;

    @Mock
    private ProjectTopicMapper topicMapper;

    @Mock
    private ProjectApplicationLogMapper logMapper;

    @Mock
    private NotificationService notificationService;

    private ProjectApplicationService applicationService;
    private ProjectTopicService topicService;

    @BeforeEach
    void setUp() {
        applicationService = new ProjectApplicationService(
                applicationMapper, topicMapper, logMapper, notificationService);
        topicService = new ProjectTopicService(topicMapper, applicationMapper);
    }

    @Test
    void fullWorkflowFromPublishToAcceptance() {
        // Step 1: Teacher creates a draft topic
        TopicForm topicForm = new TopicForm();
        topicForm.setTitle("Distributed Systems Project");
        topicForm.setDescription("Build a distributed key-value store");
        topicForm.setCategoryId(1L);
        topicForm.setMaxStudents(3);

        topicService.createTopic(topicForm, 10L, "teacher");

        verify(topicMapper).insertTopic(any(ProjectTopic.class));

        // Step 2: Teacher publishes the topic
        ProjectTopic publishedTopic = topic(TopicStatus.Draft.name());
        publishedTopic.setTitle("Distributed Systems Project");
        publishedTopic.setDescription("Build a distributed key-value store");

        when(topicMapper.selectTopicById(7L)).thenReturn(publishedTopic);

        topicService.publishTopic(7L, "teacher");

        verify(topicMapper).updateTopicStatus(7L, TopicStatus.Open.name(), "teacher");

        // Step 3: Student submits application
        ProjectTopic openTopic = topic(TopicStatus.Open.name());

        when(topicMapper.selectTopicById(5L)).thenReturn(openTopic);
        when(applicationMapper.countPendingByStudentId(20L)).thenReturn(0);
        when(applicationMapper.countActiveApplicationByTopicAndStudent(5L, 20L)).thenReturn(0);

        applicationService.submitApplication(5L, 20L, "I have experience with Redis", "student");

        verify(applicationMapper).insertApplication(any(ProjectApplication.class));

        // Step 4: Teacher reviews and accepts
        ProjectApplication pendingApp = application(31L, 5L, 20L);
        ProjectTopic topic = topic(TopicStatus.Open.name());

        when(applicationMapper.selectApplicationById(31L)).thenReturn(pendingApp);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);
        when(applicationMapper.countAcceptedByTopicId(5L)).thenReturn(0);

        applicationService.acceptApplication(31L, 10L, 10L, "teacher");

        verify(applicationMapper).acceptApplication(31L, "teacher");
        verify(applicationMapper).rejectOtherPendingApplications(
                eq(20L), eq(31L), any(), any(), any());
    }

    @Test
    void workflowRejectIfTopicNotOpen() {
        ProjectTopic closedTopic = topic(TopicStatus.Closed.name());
        when(topicMapper.selectTopicById(5L)).thenReturn(closedTopic);

        assertThatThrownBy(() ->
                applicationService.submitApplication(5L, 20L, "Apply", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This topic is not open for applications");
    }

    @Test
    void workflowRejectIfStudentAlreadyHasAccepted() {
        // This method checks if a student already has an accepted topic
        when(applicationMapper.countAgreedByStudentId(20L)).thenReturn(1);

        assertThat(applicationService.studentHasAgreedProject(20L)).isTrue();
    }

    @Test
    void workflowTeacherCannotAcceptOthersTopic() {
        ProjectApplication app = application(31L, 5L, 20L);
        ProjectTopic topic = topic(TopicStatus.Open.name());
        topic.setTeacherId(99L); // Different teacher

        when(applicationMapper.selectApplicationById(31L)).thenReturn(app);
        when(topicMapper.selectTopicById(5L)).thenReturn(topic);

        assertThatThrownBy(() ->
                applicationService.acceptApplication(31L, 10L, 10L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can only review applications for your own topics");
    }

    @Test
    void workflowCloseTopicAfterAllApplicationsHandled() {
        ProjectTopic openTopic = topic(TopicStatus.Open.name());

        when(topicMapper.selectTopicById(7L)).thenReturn(openTopic);
        when(applicationMapper.countPendingByTopicId(7L)).thenReturn(0);

        topicService.closeTopic(7L, "teacher");

        verify(topicMapper).updateTopicStatus(7L, TopicStatus.Closed.name(), "teacher");
    }

    @Test
    void workflowCannotCloseWithPendingApplications() {
        ProjectTopic openTopic = topic(TopicStatus.Open.name());

        when(topicMapper.selectTopicById(7L)).thenReturn(openTopic);
        when(applicationMapper.countPendingByTopicId(7L)).thenReturn(5);

        assertThatThrownBy(() -> topicService.closeTopic(7L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pending application");
    }

    private ProjectTopic topic(String status) {
        ProjectTopic topic = new ProjectTopic();
        topic.setTopicId(5L);
        topic.setTeacherId(10L);
        topic.setStatus(status);
        topic.setMaxStudents(3);
        topic.setTitle("Test Topic");
        topic.setDescription("Test Description");
        return topic;
    }

    private ProjectApplication application(Long applicationId, Long topicId, Long studentId) {
        ProjectApplication app = new ProjectApplication();
        app.setApplicationId(applicationId);
        app.setTopicId(topicId);
        app.setStudentId(studentId);
        app.setStatus(ApplicationStatus.Pending.name());
        return app;
    }
}
