package com.cpt202.projectselection.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatchReviewResultTest {

    @Test
    void addSuccessIncrementsCountAndAddsId() {
        BatchReviewResult result = new BatchReviewResult();

        result.addSuccess(31L);
        result.addSuccess(32L);

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getSuccessIds()).containsExactly(31L, 32L);
    }

    @Test
    void addFailureIncrementsFailureCountAndAddsMessage() {
        BatchReviewResult result = new BatchReviewResult();

        result.addFailure(31L, "Topic is full");
        result.addFailure(0L, "Select at least one");

        assertThat(result.getFailureCount()).isEqualTo(2);
        assertThat(result.getFailures()).hasSize(2);
        assertThat(result.getFailures().get(0)).contains("Application #31");
    }

    @Test
    void summaryReportsAllSuccess() {
        BatchReviewResult result = new BatchReviewResult();
        result.addSuccess(31L);
        result.addSuccess(32L);

        assertThat(result.summary()).isEqualTo("2 application(s) processed");
    }

    @Test
    void summaryReportsMixedResults() {
        BatchReviewResult result = new BatchReviewResult();
        result.addSuccess(31L);
        result.addFailure(32L, "Full");

        assertThat(result.summary()).isEqualTo("1 application(s) processed, 1 failed");
    }

    @Test
    void successIdsAreImmutable() {
        BatchReviewResult result = new BatchReviewResult();
        result.addSuccess(31L);

        List<Long> ids = result.getSuccessIds();
        assertThat(ids).isUnmodifiable();
    }

    @Test
    void failuresAreImmutable() {
        BatchReviewResult result = new BatchReviewResult();
        result.addFailure(31L, "Error");

        List<String> failures = result.getFailures();
        assertThat(failures).isUnmodifiable();
    }
}
