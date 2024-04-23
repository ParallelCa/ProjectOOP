package com.example.oopandroidapi;

import java.math.BigDecimal;

public class WorkData {

    BigDecimal selfSufficiency;
    BigDecimal employmentRate;

    public BigDecimal getEmploymentRate() {
        return employmentRate;
    }

    public BigDecimal getSelfSufficiency() {
        return selfSufficiency;
    }

    public void setSelfSufficiency(BigDecimal selfSufficiency) {
        this.selfSufficiency = selfSufficiency;
    }

    public void setEmploymentRate(BigDecimal employmentRate) {
        this.employmentRate = employmentRate;
    }
}
