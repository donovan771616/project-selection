package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTopicService {

    private final ProjectTopicMapper topicMapper;

    public ProjectTopicService(ProjectTopicMapper topicMapper) {
        this.topicMapper = topicMapper;
    }

    /** 获取所有学生可见的题目（Open 状态） */
    public List<ProjectTopic> listOpenTopics() {
        return topicMapper.selectOpenTopics();
    }

    /** 获取某位教师的所有题目 */
    public List<ProjectTopic> listTeacherTopics(Long teacherId) {
        return topicMapper.selectTopicsByTeacherId(teacherId);
    }

    /** 根据 ID 获取题目 */
    public ProjectTopic getTopic(Long topicId) {
        return topicMapper.selectTopicById(topicId);
    }

    /** 创建题目，初始状态为 Draft */
    public void createTopic(TopicForm form, Long teacherId) {
        ProjectTopic topic = new ProjectTopic();
        topic.setTeacherId(teacherId);
        topic.setTitle(form.getTitle());
        topic.setDescription(form.getDescription());
        topic.setSkills(form.getSkills());
        topic.setKeywords(form.getKeywords());
        topic.setCategoryId(form.getCategoryId());
        topic.setMaxStudents(form.getMaxStudents());
        topic.setStatus(TopicStatus.Draft.name());
        topicMapper.insertTopic(topic);
    }

    /** 更新题目基本信息 */
    public void updateTopic(TopicForm form) {
        ProjectTopic topic = new ProjectTopic();
        topic.setTopicId(form.getTopicId());
        topic.setTitle(form.getTitle());
        topic.setDescription(form.getDescription());
        topic.setSkills(form.getSkills());
        topic.setKeywords(form.getKeywords());
        topic.setCategoryId(form.getCategoryId());
        topic.setMaxStudents(form.getMaxStudents());
        topicMapper.updateTopic(topic);
    }

    /** Draft → Open */
    public void publishTopic(Long topicId) {
        ProjectTopic topic = requireTopic(topicId);
        if (!TopicStatus.Draft.name().equals(topic.getStatus())) {
            throw new IllegalArgumentException("Only draft topics can be published");
        }
        if (topic.getTitle() == null || topic.getTitle().isBlank()
                || topic.getDescription() == null || topic.getDescription().isBlank()) {
            throw new IllegalArgumentException("Title and description are required before publishing");
        }
        topicMapper.updateTopicStatus(topicId, TopicStatus.Open.name());
    }

    /** Open → Closed */
    public void closeTopic(Long topicId) {
        ProjectTopic topic = requireTopic(topicId);
        if (!TopicStatus.Open.name().equals(topic.getStatus())) {
            throw new IllegalArgumentException("Only open topics can be closed");
        }
        topicMapper.updateTopicStatus(topicId, TopicStatus.Closed.name());
    }

    /** Closed → Archived */
    public void archiveTopic(Long topicId) {
        ProjectTopic topic = requireTopic(topicId);
        if (!TopicStatus.Closed.name().equals(topic.getStatus())) {
            throw new IllegalArgumentException("Only closed topics can be archived");
        }
        topicMapper.updateTopicStatus(topicId, TopicStatus.Archived.name());
    }

    private ProjectTopic requireTopic(Long topicId) {
        ProjectTopic topic = topicMapper.selectTopicById(topicId);
        if (topic == null) {
            throw new IllegalArgumentException("Topic not found: " + topicId);
        }
        return topic;
    }
}
