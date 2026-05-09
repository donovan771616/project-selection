package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.ProjectApplication;
import com.cpt202.projectselection.domain.StatItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectApplicationMapper {

    ProjectApplication selectApplicationById(Long applicationId);

    List<ProjectApplication> selectPendingByTeacherId(Long teacherId);

    List<ProjectApplication> selectHistoryByTeacherId(Long teacherId);

    List<ProjectApplication> selectPendingByTeacherIdPaged(@Param("teacherId") Long teacherId,
                                                           @Param("keyword") String keyword,
                                                           @Param("sort") String sort,
                                                           @Param("topicId") Long topicId,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset);

    int countPendingByTeacherIdFiltered(@Param("teacherId") Long teacherId,
                                      @Param("keyword") String keyword,
                                      @Param("topicId") Long topicId);

    List<ProjectApplication> selectHistoryByTeacherIdPaged(@Param("teacherId") Long teacherId,
                                                           @Param("status") String status,
                                                           @Param("keyword") String keyword,
                                                           @Param("sort") String sort,
                                                           @Param("topicId") Long topicId,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset);

    int countHistoryByTeacherIdFiltered(@Param("teacherId") Long teacherId,
                                        @Param("status") String status,
                                        @Param("keyword") String keyword,
                                        @Param("topicId") Long topicId);

    List<ProjectApplication> selectAllApplications(@Param("status") String status,
                                                   @Param("keyword") String keyword);

    List<ProjectApplication> selectAllApplicationsPaged(@Param("status") String status,
                                                        @Param("keyword") String keyword,
                                                        @Param("sort") String sort,
                                                        @Param("limit") int limit,
                                                        @Param("offset") int offset);

    int countAllApplicationsFiltered(@Param("status") String status,
                                     @Param("keyword") String keyword);

    List<ProjectApplication> selectHistoryByStudentId(Long studentId);

    List<ProjectApplication> selectHistoryByStudentIdPaged(@Param("studentId") Long studentId,
                                                           @Param("status") String status,
                                                           @Param("keyword") String keyword,
                                                           @Param("sort") String sort,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset);

    int countHistoryByStudentIdFiltered(@Param("studentId") Long studentId,
                                        @Param("status") String status,
                                        @Param("keyword") String keyword);

    int insertApplication(ProjectApplication application);

    int countActiveApplicationByTopicAndStudent(@Param("topicId") Long topicId,
                                                @Param("studentId") Long studentId);

    int countAgreedByStudentId(Long studentId);

    int countPendingByStudentId(Long studentId);

    int countPendingByTopicId(Long topicId);

    int countAcceptedByTopicId(Long topicId);

    int acceptApplication(@Param("applicationId") Long applicationId,
                          @Param("updateBy") String updateBy);

    int rejectApplication(@Param("applicationId") Long applicationId,
                          @Param("reason") String reason,
                          @Param("rejectBy") Long rejectBy,
                          @Param("updateBy") String updateBy);

    int withdrawApplication(@Param("applicationId") Long applicationId,
                            @Param("studentId") Long studentId,
                            @Param("updateBy") String updateBy);

    int rejectOtherPendingApplications(@Param("studentId") Long studentId,
                                       @Param("acceptedApplicationId") Long acceptedApplicationId,
                                       @Param("reason") String reason,
                                       @Param("rejectBy") Long rejectBy,
                                       @Param("updateBy") String updateBy);

    int countApplicationsByStatus(String status);

    int countTopicsByStatus(String status);

    int countStudentsWithAcceptedTopic();

    int countStudentsWithoutAcceptedTopic();

    List<StatItem> selectTopicStatusCounts();

    List<StatItem> selectApplicationStatusCounts();

    List<StatItem> selectTopicCountByCategory();

    List<StatItem> selectTeacherTopicRanking();

    List<StatItem> selectPopularTopicRanking();

    List<StatItem> selectTeacherTopicStatusCounts(Long teacherId);

    List<StatItem> selectTeacherApplicationStatusCounts(Long teacherId);

    List<StatItem> selectStudentApplicationStatusCounts(Long studentId);

    int countVisibleTopics();

    int countTeacherPendingApplications(Long teacherId);

    int countTeacherAcceptedApplications(Long teacherId);

    List<StatItem> selectPendingCountByTeacherTopics(Long teacherId);

    int deleteApplication(@Param("applicationId") Long applicationId,
                          @Param("updateBy") String updateBy);
}
