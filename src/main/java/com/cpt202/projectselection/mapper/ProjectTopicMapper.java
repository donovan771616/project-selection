package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.ProjectTopic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProjectTopicMapper {

    /** 查询所有 Open 状态题目（学生可见） */
    List<ProjectTopic> selectOpenTopics();

    /** 查询某位教师的所有题目 */
    List<ProjectTopic> selectTopicsByTeacherId(Long teacherId);

    /** 根据 ID 查询单个题目 */
    ProjectTopic selectTopicById(Long topicId);

    /** 新增题目 */
    int insertTopic(ProjectTopic topic);

    /** 更新题目基本信息 */
    int updateTopic(ProjectTopic topic);

    /** 更新题目状态 */
    int updateTopicStatus(@Param("topicId") Long topicId,
                          @Param("status") String status);
}
