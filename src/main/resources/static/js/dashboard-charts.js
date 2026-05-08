(function () {
    const palette = ["#2563eb", "#16a34a", "#f59e0b", "#ef4444", "#64748b", "#7c3aed", "#0891b2", "#db2777"];

    function showEmpty(container) {
        const empty = container.parentElement.querySelector(".chart-empty");
        if (empty) {
            empty.hidden = false;
        }
    }

    function chartData(series) {
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
                label: {
                    formatter: "{b}\n{c}"
                },
                data: chartData(series)
            }]
        };
    }

    function barOption(title, series, horizontal) {
        const labels = series && series.labels ? series.labels : [];
        const values = series && series.values ? series.values : [];
        const axisLabel = {
            color: "#64748b",
            interval: 0
        };
        return {
            color: [palette[0]],
            tooltip: {
                trigger: "axis",
                axisPointer: {type: "shadow"},
                formatter: function (items) {
                    const item = items[0];
                    return item.name + ": " + item.value;
                }
            },
            grid: {
                top: 24,
                right: 22,
                bottom: horizontal ? 18 : 48,
                left: horizontal ? 94 : 36,
                containLabel: true
            },
            xAxis: horizontal
                ? {type: "value", axisLabel: axisLabel, splitLine: {lineStyle: {color: "#e8eef7"}}}
                : {type: "category", data: labels, axisLabel: axisLabel},
            yAxis: horizontal
                ? {type: "category", data: labels, axisLabel: axisLabel}
                : {type: "value", axisLabel: axisLabel, splitLine: {lineStyle: {color: "#e8eef7"}}},
            series: [{
                name: title,
                type: "bar",
                barMaxWidth: 32,
                itemStyle: {borderRadius: horizontal ? [0, 6, 6, 0] : [6, 6, 0, 0]},
                data: values
            }]
        };
    }

    function gaugeOption(title, series) {
        const data = chartData(series);
        const total = data.reduce(function (sum, item) { return sum + item.value; }, 0);
        const withTopic = data.length > 0 ? data[0].value : 0;
        const rate = total > 0 ? Math.round(withTopic / total * 100) : 0;
        return {
            color: [palette[1], "#e5e7eb"],
            series: [{
                name: title,
                type: "gauge",
                center: ["50%", "58%"],
                radius: "78%",
                startAngle: 200,
                endAngle: -20,
                min: 0,
                max: 100,
                splitNumber: 5,
                pointer: {length: "60%", width: 6, icon: "path://M12.8,0.7l12,40.1H0.7L12.8,0.7z"},
                itemStyle: {
                    color: palette[1]
                },
                progress: {
                    show: true,
                    width: 14,
                    roundCap: true
                },
                axisLine: {
                    lineStyle: {
                        width: 14,
                        roundCap: true
                    }
                },
                axisTick: {show: false},
                splitLine: {
                    length: 8,
                    lineStyle: {width: 1, color: "#fff"}
                },
                axisLabel: {
                    distance: 20,
                    color: "#64748b",
                    formatter: "{value}%"
                },
                anchor: {
                    show: true,
                    showAbove: true,
                    size: 10,
                    itemStyle: {
                        borderWidth: 2,
                        color: palette[1]
                    }
                },
                title: {
                    show: false
                },
                detail: {
                    valueAnimation: true,
                    fontSize: 28,
                    fontWeight: "600",
                    offsetCenter: [0, "30%"],
                    formatter: function (value) {
                        return value + "%";
                    },
                    color: palette[1]
                },
                data: [{value: rate, name: ""}]
            }]
        };
    }

    function renderChart(config) {
        const container = document.getElementById(config.id);
        if (!container || !window.echarts) {
            if (container) {
                showEmpty(container);
            }
            return null;
        }
        let series = config.series;
        if (typeof series === "string") {
            try {
                series = JSON.parse(series);
            } catch (e) {
                console.error("Chart JSON parse error:", config.id, series, e);
                showEmpty(container);
                return null;
            }
        }
        console.log("Chart render:", config.id, "series=", series);
        if (!series || series.empty) {
            console.log("Chart empty, showing placeholder:", config.id);
            showEmpty(container);
            return null;
        }
        const chart = echarts.init(container);
        let option;
        if (config.type === "pie") {
            option = pieOption(config.title, series);
        } else if (config.type === "gauge") {
            option = gaugeOption(config.title, series);
        } else {
            option = barOption(config.title, series, config.horizontal);
        }
        chart.setOption(option);
        const emptyEl = container.parentElement.querySelector(".chart-empty");
        if (emptyEl) {
            emptyEl.hidden = true;
        }
        return chart;
    }

    window.ProjectCharts = {
        render: function (configs) {
            const charts = configs.map(renderChart).filter(Boolean);
            window.addEventListener("resize", function () {
                charts.forEach(function (chart) {
                    chart.resize();
                });
            });
        }
    };
})();
