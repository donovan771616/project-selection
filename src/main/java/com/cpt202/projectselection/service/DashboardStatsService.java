package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.StatItem;
import com.cpt202.projectselection.domain.enums.ApplicationStatus;
import com.cpt202.projectselection.domain.enums.TopicStatus;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import com.cpt202.projectselection.mapper.SysUserMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DashboardStatsService {

    private static final Logger log = LoggerFactory.getLogger(DashboardStatsService.class);

    private static final List<String> TOPIC_STATUSES = Arrays.stream(TopicStatus.values())
            .map(Enum::name)
            .collect(Collectors.toList());
    private static final List<String> APPLICATION_STATUSES = Arrays.stream(ApplicationStatus.values())
            .map(Enum::name)
            .collect(Collectors.toList());

    private final ProjectApplicationMapper applicationMapper;
    private final ProjectTopicMapper topicMapper;
    private final SysUserMapper userMapper;

    public DashboardStatsService(ProjectApplicationMapper applicationMapper,
                                 ProjectTopicMapper topicMapper,
                                 SysUserMapper userMapper) {
        this.applicationMapper = applicationMapper;
        this.topicMapper = topicMapper;
        this.userMapper = userMapper;
    }

    public DashboardStats buildAdminStats() {
        DashboardStats stats = baseStats();
        stats.setCategoryTopics(toChartSeries(applicationMapper.selectTopicCountByCategory()));
        stats.setTeacherRanking(toChartSeries(applicationMapper.selectTeacherTopicRanking()));
        stats.setPopularTopics(toChartSeries(applicationMapper.selectPopularTopicRanking()));
        log.info("Admin stats built: topicStatus={}, applicationStatus={}",
                stats.getTopicStatus().toJson(), stats.getApplicationStatus().toJson());
        return stats;
    }

    public DashboardStats buildTeacherStats(Long teacherId) {
        DashboardStats stats = new DashboardStats();
        stats.setTopicCounts(toCountMap(applicationMapper.selectTeacherTopicStatusCounts(teacherId), TOPIC_STATUSES));
        stats.setApplicationCounts(toCountMap(applicationMapper.selectTeacherApplicationStatusCounts(teacherId), APPLICATION_STATUSES));
        stats.setTopicStatus(toChartSeries(fromCountMap(stats.getTopicCounts())));
        stats.setApplicationStatus(toChartSeries(fromCountMap(stats.getApplicationCounts())));
        stats.setPendingApplications(applicationMapper.countTeacherPendingApplications(teacherId));
        stats.setAcceptedApplications(stats.getApplicationCounts().get(ApplicationStatus.Approved.name()));
        stats.setTotalTopics(total(stats.getTopicCounts()));
        stats.setTotalApplications(total(stats.getApplicationCounts()));
        return stats;
    }

    public DashboardStats buildStudentStats(Long studentId) {
        DashboardStats stats = new DashboardStats();
        stats.setApplicationCounts(toCountMap(applicationMapper.selectStudentApplicationStatusCounts(studentId), APPLICATION_STATUSES));
        stats.setApplicationStatus(toChartSeries(fromCountMap(stats.getApplicationCounts())));
        stats.setOpenTopics(applicationMapper.countVisibleTopics());
        stats.setAcceptedApplications(stats.getApplicationCounts().get(ApplicationStatus.Approved.name()));
        stats.setPendingApplications(stats.getApplicationCounts().get(ApplicationStatus.Pending.name()));
        stats.setTotalApplications(total(stats.getApplicationCounts()));
        stats.setHasAcceptedTopic(stats.getApplicationCounts().get(ApplicationStatus.Approved.name()) > 0);
        return stats;
    }

    public DashboardStats buildReportStats() {
        return buildAdminStats();
    }

    public ChartSeries toChartSeries(List<StatItem> items) {
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        int total = 0;
        if (items != null) {
            for (StatItem item : items) {
                labels.add(item.getLabel());
                int value = item.getValue() == null ? 0 : item.getValue();
                values.add(value);
                total += value;
            }
        }
        return new ChartSeries(labels, values, total);
    }

    public Map<String, Integer> toCountMap(List<StatItem> items, List<String> expectedLabels) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String label : expectedLabels) {
            counts.put(label, 0);
        }
        if (items != null) {
            for (StatItem item : items) {
                counts.put(item.getLabel(), item.getValue() == null ? 0 : item.getValue());
            }
        }
        return counts;
    }

    private DashboardStats baseStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTopicCounts(toCountMap(applicationMapper.selectTopicStatusCounts(), TOPIC_STATUSES));
        stats.setApplicationCounts(toCountMap(applicationMapper.selectApplicationStatusCounts(), APPLICATION_STATUSES));
        stats.setTopicStatus(toChartSeries(fromCountMap(stats.getTopicCounts())));
        stats.setApplicationStatus(toChartSeries(fromCountMap(stats.getApplicationCounts())));
        stats.setTotalTopics(total(stats.getTopicCounts()));
        stats.setTotalApplications(total(stats.getApplicationCounts()));
        int totalStudents = userMapper.countByRoleKey("student");
        int withAccepted = stats.getApplicationCounts().getOrDefault(ApplicationStatus.Approved.name(), 0);
        int withoutAccepted = Math.max(0, totalStudents - withAccepted);
        stats.setStudentsWithAcceptedTopic(withAccepted);
        stats.setStudentsWithoutAcceptedTopic(withoutAccepted);
        stats.setStudentAssignment(toChartSeries(Arrays.asList(
                new StatItem("Assigned", withAccepted),
                new StatItem("Unassigned", withoutAccepted)
        )));
        stats.setOpenTopics(applicationMapper.countVisibleTopics());
        stats.setPendingApplications(stats.getApplicationCounts().get(ApplicationStatus.Pending.name()));
        stats.setAcceptedApplications(stats.getApplicationCounts().get(ApplicationStatus.Approved.name()));
        return stats;
    }

    private List<StatItem> fromCountMap(Map<String, Integer> counts) {
        return counts.entrySet().stream()
                .map(entry -> new StatItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private int total(Map<String, Integer> counts) {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static class DashboardStats {
        private Map<String, Integer> topicCounts = new LinkedHashMap<>();
        private Map<String, Integer> applicationCounts = new LinkedHashMap<>();
        private ChartSeries topicStatus = ChartSeries.empty();
        private ChartSeries applicationStatus = ChartSeries.empty();
        private ChartSeries categoryTopics = ChartSeries.empty();
        private ChartSeries teacherRanking = ChartSeries.empty();
        private ChartSeries popularTopics = ChartSeries.empty();
        private ChartSeries studentAssignment = ChartSeries.empty();
        private int totalTopics;
        private int totalApplications;
        private int openTopics;
        private int pendingApplications;
        private int acceptedApplications;
        private int studentsWithAcceptedTopic;
        private int studentsWithoutAcceptedTopic;
        private boolean hasAcceptedTopic;

        public Map<String, Integer> getTopicCounts() { return topicCounts; }
        public void setTopicCounts(Map<String, Integer> topicCounts) { this.topicCounts = topicCounts; }
        public Map<String, Integer> getApplicationCounts() { return applicationCounts; }
        public void setApplicationCounts(Map<String, Integer> applicationCounts) { this.applicationCounts = applicationCounts; }
        public ChartSeries getTopicStatus() { return topicStatus; }
        public void setTopicStatus(ChartSeries topicStatus) { this.topicStatus = topicStatus; }
        public ChartSeries getApplicationStatus() { return applicationStatus; }
        public void setApplicationStatus(ChartSeries applicationStatus) { this.applicationStatus = applicationStatus; }
        public ChartSeries getCategoryTopics() { return categoryTopics; }
        public void setCategoryTopics(ChartSeries categoryTopics) { this.categoryTopics = categoryTopics; }
        public ChartSeries getTeacherRanking() { return teacherRanking; }
        public void setTeacherRanking(ChartSeries teacherRanking) { this.teacherRanking = teacherRanking; }
        public ChartSeries getPopularTopics() { return popularTopics; }
        public void setPopularTopics(ChartSeries popularTopics) { this.popularTopics = popularTopics; }
        public ChartSeries getStudentAssignment() { return studentAssignment; }
        public void setStudentAssignment(ChartSeries studentAssignment) { this.studentAssignment = studentAssignment; }
        public int getTotalTopics() { return totalTopics; }
        public void setTotalTopics(int totalTopics) { this.totalTopics = totalTopics; }
        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
        public int getOpenTopics() { return openTopics; }
        public void setOpenTopics(int openTopics) { this.openTopics = openTopics; }
        public int getPendingApplications() { return pendingApplications; }
        public void setPendingApplications(int pendingApplications) { this.pendingApplications = pendingApplications; }
        public int getAcceptedApplications() { return acceptedApplications; }
        public void setAcceptedApplications(int acceptedApplications) { this.acceptedApplications = acceptedApplications; }
        public int getStudentsWithAcceptedTopic() { return studentsWithAcceptedTopic; }
        public void setStudentsWithAcceptedTopic(int v) { this.studentsWithAcceptedTopic = v; }
        public int getStudentsWithoutAcceptedTopic() { return studentsWithoutAcceptedTopic; }
        public void setStudentsWithoutAcceptedTopic(int v) { this.studentsWithoutAcceptedTopic = v; }
        public boolean isHasAcceptedTopic() { return hasAcceptedTopic; }
        public void setHasAcceptedTopic(boolean hasAcceptedTopic) { this.hasAcceptedTopic = hasAcceptedTopic; }
    }

    public static class ChartSeries {
        private final List<String> labels;
        private final List<Integer> values;
        private final int total;

        public ChartSeries(List<String> labels, List<Integer> values, int total) {
            this.labels = labels;
            this.values = values;
            this.total = total;
        }

        public static ChartSeries empty() {
            return new ChartSeries(new ArrayList<>(), new ArrayList<>(), 0);
        }

        public List<String> getLabels() { return labels; }
        public List<Integer> getValues() { return values; }
        public int getTotal() { return total; }

        public boolean isEmpty() {
            return labels.isEmpty() || total == 0;
        }

        public boolean getEmpty() {
            return isEmpty();
        }

        @Override
        public String toString() {
            return "ChartSeries{labels=" + labels + ", values=" + values + ", total=" + total + '}';
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"labels\": [");
            for (int i = 0; i < labels.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("\"").append(labels.get(i)).append("\"");
            }
            sb.append("], \"values\": [");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(values.get(i));
            }
            sb.append("], \"total\": ").append(total);
            sb.append(", \"empty\": ").append(isEmpty());
            sb.append("}");
            return sb.toString();
        }
    }
}
