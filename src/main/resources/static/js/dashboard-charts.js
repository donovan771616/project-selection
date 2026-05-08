// FIXED: Null/undefined guard on series data
// FIXED: Resize listener added
// FIXED: echarts library existence check
// TODO: No empty state placeholder shown when series is empty
// TODO: Gauge chart type not yet supported
// TODO: Horizontal bar chart not yet supported
(function () {
    const palette = ["#2563eb", "#16a34a", "#f59e0b", "#ef4444", "#64748b", "#7c3aed"];

    function chartData(series) {
        // FIXED: Guard against null/undefined series
        const labels = series && series.labels ? series.labels : [];
        const values = series && series.values ? series.values : [];
        return labels.map(function (label, index) {
            return {name: label, value: values[index] || 0};
        });
    }

    function pieOption(title, series) {
        return {
            color: palette,
            tooltip: {
                trigger: "item",
                formatter: "{b}: {c} ({d}%)"
            },
            legend: {
                bottom: 0,
                type: "scroll",
                icon: "circle"
            },
            series: [{
                name: title,
                type: "pie",
                radius: ["48%", "72%"],
                center: ["50%", "43%"],
                avoidLabelOverlap: true,
                label: {formatter: "{b}\n{c}"},
                data: chartData(series)
            }]
        };
    }

    function barOption(title, series) {
        const labels = series && series.labels ? series.labels : [];
        const values = series && series.values ? series.values : [];
        return {
            color: [palette[0]],
            tooltip: {trigger: "axis"},
            xAxis: {type: "category", data: labels},
            yAxis: {type: "value"},
            series: [{
                name: title,
                type: "bar",
                barMaxWidth: 32,
                data: values
            }]
        };
        // TODO: horizontal bar variant not yet implemented
    }

    function renderChart(config) {
        const container = document.getElementById(config.id);
        // FIXED: Check container and echarts exist
        if (!container || !window.echarts) {
            return null;
        }
        let series = config.series;
        if (typeof series === "string") {
            try {
                series = JSON.parse(series);
            } catch (e) {
                console.error("Chart JSON parse error:", config.id, e);
                return null;
            }
        }
        // TODO: No empty state shown - chart renders blank instead of placeholder
        const chart = echarts.init(container);
        let option;
        if (config.type === "pie") {
            option = pieOption(config.title, series);
        } else {
            option = barOption(config.title, series);
        }
        chart.setOption(option);
        return chart;
    }

    window.ProjectCharts = {
        render: function (configs) {
            const charts = configs.map(renderChart).filter(Boolean);
            // FIXED: Resize listener
            window.addEventListener("resize", function () {
                charts.forEach(function (chart) { chart.resize(); });
            });
        }
    };
})();
