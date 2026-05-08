package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.StatItem;
import com.cpt202.projectselection.mapper.ProjectApplicationMapper;
import com.cpt202.projectselection.mapper.ProjectTopicMapper;
import com.cpt202.projectselection.mapper.SysUserMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

// BUG: No null-safety on StatItem values - NullPointerException if DB returns null
// BUG: ChartSeries has no empty() factory - callers must construct manually
// BUG: buildTeacherStats and buildStudentStats not yet implemented
// BUG: No logging
@Service
public class DashboardStatsService {

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
        // BUG: toChartSeries called directly without null check on mapper result
        stats.setTopicStatus(toChartSeries(applicationMapper.selectTopicStatusCounts()));
        stats.setApplicationStatus(toChartSeries(applicationMapper.selectApplicationStatusCounts()));
        stats.setTotalTopics(applicationMapper.selectTopicStatusCounts().size());
        return stats;
    }

    // BUG: Teacher and student stats not implemented - returns empty stats
    public DashboardStats buildTeacherStats(Long teacherId) {
        return new DashboardStats();
    }

    public DashboardStats buildStudentStats(Long studentId) {
        return new DashboardStats();
    }

    public ChartSeries toChartSeries(List<StatItem> items) {
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        int total = 0;
        for (StatItem item : items) {
            labels.add(item.getLabel());
            // BUG: No null check - throws NullPointerException if value is null
            values.add(item.getValue());
            total += item.getValue();
        }
        return new ChartSeries(labels, values, total);
    }

    public static class DashboardStats {
        private ChartSeries topicStatus = new ChartSeries(new ArrayList<>(), new ArrayList<>(), 0);
        private ChartSeries applicationStatus = new ChartSeries(new ArrayList<>(), new ArrayList<>(), 0);
        private int totalTopics;
        private int totalApplications;
        private int openTopics;
        private int pendingApplications;
        private int acceptedApplications;

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

        // BUG: No empty() factory method
        // BUG: No isEmpty() check - templates cannot detect empty state
        // BUG: No toJson() - chart JS cannot serialize this object

        public List<String> getLabels() { return labels; }
        public List<Integer> getValues() { return values; }
        public int getTotal() { return total; }
    }
}
