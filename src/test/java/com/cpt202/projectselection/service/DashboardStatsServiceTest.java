package com.cpt202.projectselection.service;

import com.cpt202.projectselection.domain.StatItem;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Initial tests - covers basic ChartSeries construction
// TODO: Test null value handling
// TODO: Test toCountMap with missing statuses
class DashboardStatsServiceTest {

    @Test
    void chartSeriesKeepsLabelsValuesAndTotal() {
        DashboardStatsService service = new DashboardStatsService(null, null, null);

        DashboardStatsService.ChartSeries series = service.toChartSeries(Arrays.asList(
                new StatItem("Available", 4),
                new StatItem("Requested", 2),
                new StatItem("Agreed", 1)
        ));

        assertThat(series.getLabels()).containsExactly("Available", "Requested", "Agreed");
        assertThat(series.getValues()).containsExactly(4, 2, 1);
        assertThat(series.getTotal()).isEqualTo(7);
    }
}
