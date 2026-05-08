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
import org.springframework.stereotype.Service;

// FIXED: Null-safety on StatItem values
// FIXED: ChartSeries.empty() factory method added
// FIXED: toCountMap() added for keyed status counts
// TODO: buildTeacherStats and buildStudentStats still incomplete
// TODO: No logging yet
// TODO: ChartSeries missing toJson() and isEmpty() - templates cannot detect empty state
@Service
public class DashboardStatsService {

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
        DashboardStats stats = new DashboardStats();
        stats.setTopicCounts(toCountMap(applicationMapper.selectTopicStatusCounts(), TOPIC_STATUSES));
        stats.setApplicationCounts(toCountMap(applicationMapper.selectApplicationStatusCounts(), APPLICATION_STATUSES));
        stats.setTopicStatus(toChartSeries(fromCountMap(stats.getTopicCounts())));
        stats.setApplicationStatus(toChartSeries(fromCountMap(stats.getApplicationCounts())));
        stats.setTotalTopics(total(stats.getTopicCounts()));
        stats.setTotalApplications(total(stats.getApplicationCounts()));
        stats.setOpenTopics(applicationMapper.countVisibleTopics());
        stats.setPendingApplications(stats.getApplicationCounts().get(ApplicationStatus.Pending.name()));
        stats.setAcceptedApplications(stats.getApplicationCounts().get(ApplicationStatus.Approved.name()));
        return stats;
    }

    // TODO: Teacher-specific stats not yet implemented
    public DashboardStats buildTeacherStats(Long teacherId) {
        return new DashboardStats();
    }

    // TODO: Student-specific stats not yet implemented
    public DashboardStats buildStudentStats(Long studentId) {
        return new DashboardStats();
    }

    public ChartSeries toChartSeries(List<StatItem> items) {
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        int total = 0;
        if (items != null) {
            for (StatItem item : items) {
                labels.add(item.getLabel());
                // FIXED: Null-safe value handling
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
        private int totalTopics;
        private int totalApplications;
        private int openTopics;
        private int pendingApplications;
        private int acceptedApplications;

        public Map<String, Integer> getTopicCounts() { return topicCounts; }
        public void setTopicCounts(Map<String, Integer> topicCounts) { this.topicCounts = topicCounts; }
        public Map<String, Integer> getApplicationCounts() { return applicationCounts; }
        public void setApplicationCounts(Map<String, Integer> applicationCounts) { this.applicationCounts = applicationCounts; }
        public ChartSeries getTopicStatus() { return topicStatus; }
        public void setTopicStatus(ChartSeries topicStatus) { this.topicStatus = topicStatus; }
        public ChartSeries getApplicationStatus() { return applicationStatus; }
        public void setApplicationStatus(ChartSeries applicationStatus) { this.applicationStatus = applicationStatus; }
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

        // FIXED: empty() factory method
        public static ChartSeries empty() {
            return new ChartSeries(new ArrayList<>(), new ArrayList<>(), 0);
        }

        public List<String> getLabels() { return labels; }
        public List<Integer> getValues() { return values; }
        public int getTotal() { return total; }

        // TODO: isEmpty() not yet exposed - templates cannot detect empty state
        // TODO: toJson() not yet implemented - chart JS cannot serialize
    }
}
