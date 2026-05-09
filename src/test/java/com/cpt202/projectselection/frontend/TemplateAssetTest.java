package com.cpt202.projectselection.frontend;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateAssetTest {

    @Test
    void chartPagesUseLocalEchartsAsset() throws Exception {
        List<Path> chartPages = Arrays.asList(
                Path.of("src/main/resources/templates/dashboard/admin.html"),
                Path.of("src/main/resources/templates/dashboard/teacher.html"),
                Path.of("src/main/resources/templates/dashboard/student.html"),
                Path.of("src/main/resources/templates/project/reports.html")
        );

        for (Path page : chartPages) {
            String html = Files.readString(page, StandardCharsets.UTF_8);

            assertThat(html)
                    .doesNotContain("https://cdn.jsdelivr.net/npm/echarts")
                    .contains("th:src=\"@{/vendor/echarts/echarts.min.js}\"");
        }
    }

    @Test
    void sharedLayoutUsesStructuredNavigationAndSignOutControl() throws Exception {
        String html = Files.readString(
                Path.of("src/main/resources/templates/fragments/layout.html"),
                StandardCharsets.UTF_8
        );

        assertThat(html)
                .contains("class=\"nav-link")
                .contains("class=\"nav-section\"")
                .contains("class=\"signout-button\"");
    }

    @Test
    void workflowPagesExposeBatchPaginationAndTreeControls() throws Exception {
        String applications = Files.readString(
                Path.of("src/main/resources/templates/project/applications.html"),
                StandardCharsets.UTF_8
        );
        String categories = Files.readString(
                Path.of("src/main/resources/templates/project/categories.html"),
                StandardCharsets.UTF_8
        );
        String topics = Files.readString(
                Path.of("src/main/resources/templates/project/topics.html"),
                StandardCharsets.UTF_8
        );

        assertThat(applications)
                .contains("batch-toolbar")
                .contains("pagination")
                .contains("Withdraw");
        assertThat(categories)
                .contains("category-tree")
                .contains("Root category");
        assertThat(topics)
                .contains("returnUrl")
                .contains("pagination");
    }
}
