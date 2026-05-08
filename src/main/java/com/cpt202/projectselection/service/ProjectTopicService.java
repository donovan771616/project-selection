package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.ProjectTopic;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectTopicService {

    private final ProjectTopicMapper topicMapper;
    private final ProjectApplicationMapper applicationMapper;

    public ProjectTopicService(ProjectTopicMapper topicMapper, ProjectApplicationMapper applicationMapper) {
        this.topicMapper = topicMapper;
        this.applicationMapper = applicationMapper;
    }

    public List<ProjectTopic> listVisibleTopics(String keyword, Long categoryId, String teacherName) {
        return topicMapper.selectVisibleTopics(keyword, categoryId, teacherName);
    }

    public PageResult<ProjectTopic> listVisibleTopicsPage(String keyword, Long categoryId, String teacherName,
                                                          Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return new PageResult<>(
                topicMapper.selectVisibleTopicsPaged(keyword, categoryId, teacherName,
                        pageRequest.getSize(), pageRequest.getOffset()),
                pageRequest.getPage(),
                pageRequest.getSize(),
                topicMapper.countVisibleTopicsFiltered(keyword, categoryId, teacherName)
        );
    }

    public List<ProjectTopic> listAllTopics(String keyword, Long categoryId, String teacherName) {
        return topicMapper.selectAllTopics(keyword, categoryId, teacherName);
    }

    public PageResult<ProjectTopic> listAllTopicsPage(String keyword, Long categoryId, String teacherName,
                                                      Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return new PageResult<>(
                topicMapper.selectAllTopicsPaged(keyword, categoryId, teacherName,
                        pageRequest.getSize(), pageRequest.getOffset()),
                pageRequest.getPage(),
                pageRequest.getSize(),
                topicMapper.countAllTopicsFiltered(keyword, categoryId, teacherName)
        );
    }

    public List<ProjectTopic> listTeacherTopics(Long teacherId) {
        return topicMapper.selectTopicsByTeacherId(teacherId);
    }

    public PageResult<ProjectTopic> listTeacherTopicsPage(Long teacherId, String keyword, Long categoryId,
                                                          String status, Integer page, Integer size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return new PageResult<>(
                topicMapper.selectTopicsByTeacherIdPaged(teacherId, keyword, categoryId, status,
                        pageRequest.getSize(), pageRequest.getOffset()),
                pageRequest.getPage(),
                pageRequest.getSize(),
                topicMapper.countTopicsByTeacherIdFiltered(teacherId, keyword, categoryId, status)
        );
    }

    public ProjectTopic getTopic(Long topicId) {
        return topicMapper.selectTopicById(topicId);
    }

    public void createTopic(TopicForm form, Long teacherId, String operator) {
        ProjectTopic topic = new ProjectTopic();
        copyForm(form, topic);
        topic.setTeacherId(teacherId);
        topic.setStatus(TopicStatus.Draft.name());
        topic.setCreateBy(operator);
        topicMapper.insertTopic(topic);
    }

    public void updateTopic(TopicForm form, String operator) {
        ProjectTopic topic = new ProjectTopic();
        copyForm(form, topic);
        topic.setUpdateBy(operator);
        topicMapper.updateTopic(topic);
    }

    public void publishTopic(Long topicId, String operator) {
        ProjectTopic topic = requireTopic(topicId);
        if (!TopicStatus.Draft.name().equals(topic.getStatus())) {
            throw new IllegalArgumentException("Only draft topics can be published");
        }
        if (isBlank(topic.getTitle()) || isBlank(topic.getDescription())) {
            throw new IllegalArgumentException("Title and description are required before publishing");
        }
        topicMapper.updateTopicStatus(topicId, TopicStatus.Open.name(), operator);
    }

    public void closeTopic(Long topicId, String operator) {
        ProjectTopic topic = requireTopic(topicId);
        if (!TopicStatus.Open.name().equals(topic.getStatus())) {
            throw new IllegalArgumentException("Only open topics can be closed");
        }
        int pendingCount = applicationMapper.countPendingByTopicId(topicId);
        if (pendingCount > 0) {
            throw new IllegalArgumentException("Cannot close topic: there are " + pendingCount + " pending application(s) to review. Please review or reject them first.");
        }
        topicMapper.updateTopicStatus(topicId, TopicStatus.Closed.name(), operator);
    }

    public void archiveTopic(Long topicId, String operator) {
        ProjectTopic topic = requireTopic(topicId);
        if (!TopicStatus.Closed.name().equals(topic.getStatus())) {
            throw new IllegalArgumentException("Only closed topics can be archived");
        }
        topicMapper.updateTopicStatus(topicId, TopicStatus.Archived.name(), operator);
    }

    private ProjectTopic requireTopic(Long topicId) {
        ProjectTopic topic = topicMapper.selectTopicById(topicId);
        if (topic == null) {
            throw new IllegalArgumentException("Topic does not exist");
        }
        return topic;
    }

    private void copyForm(TopicForm form, ProjectTopic topic) {
        topic.setTopicId(form.getTopicId());
        topic.setTitle(form.getTitle());
        topic.setDescription(form.getDescription());
        topic.setSkills(form.getSkills());
        topic.setKeywords(form.getKeywords());
        topic.setCategoryId(form.getCategoryId());
        topic.setMaxStudents(form.getMaxStudents());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
