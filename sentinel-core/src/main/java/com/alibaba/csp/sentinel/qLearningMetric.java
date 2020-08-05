package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class qLearningMetric {
    public long getCt() {
        return ct;
    }

    public void setCt(long ct) {
        this.ct = ct;
    }

    long ct;

}
