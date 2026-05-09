package com.cpt202.projectselection.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchReviewResult {

    private int successCount;
    private final List<Long> successIds = new ArrayList<>();
    private final List<String> failures = new ArrayList<>();

    public void addSuccess(Long applicationId) {
        successCount++;
        successIds.add(applicationId);
    }

    public void addFailure(Long applicationId, String message) {
        failures.add("Application #" + applicationId + ": " + message);
    }

    public int getSuccessCount() {
        return successCount;
    }

    public List<Long> getSuccessIds() {
        return Collections.unmodifiableList(successIds);
    }

    public int getFailureCount() {
        return failures.size();
    }

    public List<String> getFailures() {
        return Collections.unmodifiableList(failures);
    }

    public String summary() {
        if (getFailureCount() == 0) {
            return successCount + " application(s) processed";
        }
        return successCount + " application(s) processed, " + getFailureCount() + " failed";
    }
}
