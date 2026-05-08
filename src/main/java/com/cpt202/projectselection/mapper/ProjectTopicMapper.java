package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.ProjectTopic;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectTopicMapper {

    List<ProjectTopic> selectVisibleTopics(@Param("keyword") String keyword,
                                           @Param("categoryId") Long categoryId,
                                           @Param("teacherName") String teacherName);

    List<ProjectTopic> selectVisibleTopicsPaged(@Param("keyword") String keyword,
                                                @Param("categoryId") Long categoryId,
                                                @Param("teacherName") String teacherName,
                                                @Param("limit") int limit,
                                                @Param("offset") int offset);

    int countVisibleTopicsFiltered(@Param("keyword") String keyword,
                                   @Param("categoryId") Long categoryId,
                                   @Param("teacherName") String teacherName);

    List<ProjectTopic> selectAllTopics(@Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId,
                                       @Param("teacherName") String teacherName);

    List<ProjectTopic> selectAllTopicsPaged(@Param("keyword") String keyword,
                                            @Param("categoryId") Long categoryId,
                                            @Param("teacherName") String teacherName,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    int countAllTopicsFiltered(@Param("keyword") String keyword,
                               @Param("categoryId") Long categoryId,
                               @Param("teacherName") String teacherName);

    List<ProjectTopic> selectTopicsByTeacherId(Long teacherId);

    List<ProjectTopic> selectTopicsByTeacherIdPaged(@Param("teacherId") Long teacherId,
                                                     @Param("keyword") String keyword,
                                                     @Param("categoryId") Long categoryId,
                                                     @Param("status") String status,
                                                     @Param("limit") int limit,
                                                     @Param("offset") int offset);

    int countTopicsByTeacherIdFiltered(@Param("teacherId") Long teacherId,
                                       @Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId,
                                       @Param("status") String status);

    ProjectTopic selectTopicById(Long topicId);

    int insertTopic(ProjectTopic topic);

    int updateTopic(ProjectTopic topic);

    int updateTopicStatus(@Param("topicId") Long topicId,
                          @Param("status") String status,
                          @Param("updateBy") String updateBy);
}
