package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.enums.TopicStatus;
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
class ProjectTopicServiceLifecycleTest {

    @Mock
    private ProjectTopicMapper topicMapper;

    @Mock
    private ProjectApplicationMapper applicationMapper;

    private ProjectTopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new ProjectTopicService(topicMapper, applicationMapper);
    }

    @Test
    void createTopicSetsStatusToDraft() {
        TopicForm form = new TopicForm();
        form.setTitle("Machine Learning Project");
        form.setDescription("Build an ML model");
        form.setCategoryId(1L);
        form.setMaxStudents(3);

        topicService.createTopic(form, 10L, "teacher");

        ArgumentCaptor<ProjectTopic> captor = ArgumentCaptor.forClass(ProjectTopic.class);
        verify(topicMapper).insertTopic(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TopicStatus.Draft.name());
        assertThat(captor.getValue().getTeacherId()).isEqualTo(10L);
        assertThat(captor.getValue().getCreateBy()).isEqualTo("teacher");
    }

    @Test
    void publishTopicRejectsNonDraftTopic() {
        ProjectTopic topic = createTopic(TopicStatus.Open.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        assertThatThrownBy(() -> topicService.publishTopic(7L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only draft topics can be published");
    }

    @Test
    void publishTopicRejectsTopicWithEmptyTitle() {
        ProjectTopic topic = createTopic(TopicStatus.Draft.name());
        topic.setTitle("");
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        assertThatThrownBy(() -> topicService.publishTopic(7L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title and description are required before publishing");
    }

    @Test
    void publishTopicRejectsTopicWithEmptyDescription() {
        ProjectTopic topic = createTopic(TopicStatus.Draft.name());
        topic.setDescription(null);
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        assertThatThrownBy(() -> topicService.publishTopic(7L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title and description are required before publishing");
    }

    @Test
    void publishTopicSucceedsForValidDraftTopic() {
        ProjectTopic topic = createTopic(TopicStatus.Draft.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        topicService.publishTopic(7L, "teacher");

        verify(topicMapper).updateTopicStatus(7L, TopicStatus.Open.name(), "teacher");
    }

    @Test
    void closeTopicRejectsNonOpenTopic() {
        ProjectTopic topic = createTopic(TopicStatus.Draft.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        assertThatThrownBy(() -> topicService.closeTopic(7L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only open topics can be closed");
    }

    @Test
    void closeTopicRejectsTopicWithPendingApplications() {
        ProjectTopic topic = createTopic(TopicStatus.Open.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);
        when(applicationMapper.countPendingByTopicId(7L)).thenReturn(3);

        assertThatThrownBy(() -> topicService.closeTopic(7L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pending application");
    }

    @Test
    void closeTopicSucceedsForOpenTopicWithNoPending() {
        ProjectTopic topic = createTopic(TopicStatus.Open.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);
        when(applicationMapper.countPendingByTopicId(7L)).thenReturn(0);

        topicService.closeTopic(7L, "teacher");

        verify(topicMapper).updateTopicStatus(7L, TopicStatus.Closed.name(), "teacher");
    }

    @Test
    void archiveTopicRejectsNonClosedTopic() {
        ProjectTopic topic = createTopic(TopicStatus.Open.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        assertThatThrownBy(() -> topicService.archiveTopic(7L, "teacher"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only closed topics can be archived");
    }

    @Test
    void archiveTopicSucceedsForClosedTopic() {
        ProjectTopic topic = createTopic(TopicStatus.Closed.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        topicService.archiveTopic(7L, "teacher");

        verify(topicMapper).updateTopicStatus(7L, TopicStatus.Archived.name(), "teacher");
    }

    @Test
    void updateTopicCopiesAllFields() {
        TopicForm form = new TopicForm();
        form.setTopicId(7L);
        form.setTitle("Updated Title");
        form.setDescription("Updated description");
        form.setSkills("Java, Python");
        form.setKeywords("ML, AI");
        form.setCategoryId(2L);
        form.setMaxStudents(5);

        topicService.updateTopic(form, "teacher");

        ArgumentCaptor<ProjectTopic> captor = ArgumentCaptor.forClass(ProjectTopic.class);
        verify(topicMapper).updateTopic(captor.capture());
        ProjectTopic updated = captor.getValue();
        assertThat(updated.getTopicId()).isEqualTo(7L);
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getSkills()).isEqualTo("Java, Python");
        assertThat(updated.getKeywords()).isEqualTo("ML, AI");
        assertThat(updated.getCategoryId()).isEqualTo(2L);
        assertThat(updated.getMaxStudents()).isEqualTo(5);
        assertThat(updated.getUpdateBy()).isEqualTo("teacher");
    }

    @Test
    void getTopicReturnsTopicFromMapper() {
        ProjectTopic topic = createTopic(TopicStatus.Open.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        ProjectTopic result = topicService.getTopic(7L);

        assertThat(result).isEqualTo(topic);
    }

    private ProjectTopic createTopic(String status) {
        ProjectTopic topic = new ProjectTopic();
        topic.setTopicId(7L);
        topic.setTitle("A complete topic");
        topic.setDescription("A complete description");
        topic.setStatus(status);
        return topic;
    }
}
