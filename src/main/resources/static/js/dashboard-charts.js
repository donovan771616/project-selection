// BUG: No empty/null data guard - echarts throws if series is empty
// BUG: No resize listener - charts break on window resize
// BUG: No gauge chart support
(function () {
    const palette = ["#2563eb", "#16a34a", "#f59e0b", "#ef4444", "#64748b"];

    function chartData(series) {
        const labels = series.labels;
        const values = series.values;
        return labels.map(function (label, index) {
            return {name: label, value: values[index]};
        });
    }

    function pieOption(title, series) {
        return {
            color: palette,
            tooltip: {trigger: "item"},
            series: [{
                name: title,
                type: "pie",
                radius: ["48%", "72%"],
                // BUG: No avoidLabelOverlap - labels overlap on small datasets
                data: chartData(series)
            }]
        };
    }

    function barOption(title, series) {
        const labels = series.labels;
        const values = series.values;
        return {
            color: [palette[0]],
            tooltip: {trigger: "axis"},
            xAxis: {type: "category", data: labels},
            yAxis: {type: "value"},
            series: [{
                name: title,
                type: "bar",
                data: values
            }]
        };
    }

    function renderChart(config) {
        const container = document.getElementById(config.id);
        // BUG: No check for echarts library existence
        // BUG: No empty series guard - crashes if series.labels is empty
        const chart = echarts.init(container);
        let option;
        if (config.type === "pie") {
            option = pieOption(config.title, config.series);
        } else {
            option = barOption(config.title, config.series);
        }
        chart.setOption(option);
        return chart;
    }

    window.ProjectCharts = {
        render: function (configs) {
            // BUG: No resize handling - charts become distorted on window resize
            configs.forEach(renderChart);
        }
    };
})();
