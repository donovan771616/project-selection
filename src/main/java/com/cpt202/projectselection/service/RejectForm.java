package com.cpt202.projectselection.service;

import javax.validation.constraints.Size;

public class RejectForm {

    @Size(min = 10, max = 200, message = "Reason must be 10-200 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
