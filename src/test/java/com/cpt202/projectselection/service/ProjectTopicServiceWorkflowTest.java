package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectTopicServiceWorkflowTest {

    @Mock
    private ProjectTopicMapper topicMapper;

    private ProjectTopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new ProjectTopicService(topicMapper);
    }

    @Test
    void publishOnlyAllowsDraftTopics() {
        ProjectTopic topic = makeTopic(TopicStatus.Closed.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        assertThatThrownBy(() -> topicService.publishTopic(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only draft topics can be published");
    }

    @Test
    void closeOnlyAllowsOpenTopics() {
        ProjectTopic topic = makeTopic(TopicStatus.Draft.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        assertThatThrownBy(() -> topicService.closeTopic(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only open topics can be closed");
    }

    @Test
    void closeChangesOpenTopicToClosed() {
        ProjectTopic topic = makeTopic(TopicStatus.Open.name());
        when(topicMapper.selectTopicById(7L)).thenReturn(topic);

        topicService.closeTopic(7L);

        verify(topicMapper).updateTopicStatus(7L, TopicStatus.Closed.name());
    }

    private ProjectTopic makeTopic(String status) {
        ProjectTopic topic = new ProjectTopic();
        topic.setTopicId(7L);
        topic.setTitle("A complete topic");
        topic.setDescription("A complete description");
        topic.setStatus(status);
        return topic;
    }
}
