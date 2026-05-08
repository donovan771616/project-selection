package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.StatItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardStatsServiceTest {

    @Test
    void chartSeriesKeepsLabelsValuesTotalAndEmptyState() {
        DashboardStatsService service = new DashboardStatsService(null, null, null);

        DashboardStatsService.ChartSeries series = service.toChartSeries(Arrays.asList(
                new StatItem("Available", 4),
                new StatItem("Requested", 2),
                new StatItem("Agreed", 1)
        ));

        assertThat(series.getLabels()).containsExactly("Available", "Requested", "Agreed");
        assertThat(series.getValues()).containsExactly(4, 2, 1);
        assertThat(series.getTotal()).isEqualTo(7);
        assertThat(series.isEmpty()).isFalse();
    }

    @Test
    void chartSeriesTreatsNullValuesAsZero() {
        DashboardStatsService service = new DashboardStatsService(null, null, null);

        DashboardStatsService.ChartSeries series = service.toChartSeries(Collections.singletonList(
                new StatItem("Draft", null)
        ));

        assertThat(series.getLabels()).containsExactly("Draft");
        assertThat(series.getValues()).containsExactly(0);
        assertThat(series.getTotal()).isZero();
        assertThat(series.isEmpty()).isTrue();
    }

    @Test
    void keyedCountsExposeMissingStatusesAsZero() {
        DashboardStatsService service = new DashboardStatsService(null, null, null);

        Map<String, Integer> counts = service.toCountMap(
                Arrays.asList(new StatItem("Pending", 3)),
                Arrays.asList("Pending", "Accepted", "Rejected", "Withdrawn")
        );

        assertThat(counts)
                .containsEntry("Pending", 3)
                .containsEntry("Accepted", 0)
                .containsEntry("Rejected", 0)
                .containsEntry("Withdrawn", 0);
    }
}
