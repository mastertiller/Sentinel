package com.alibaba.csp.sentinel;

import java.util.concurrent.atomic.AtomicLong;

public class QLearningMetric {
    public long getCt() {
        return ct.get();
    }

    public void setCt(long t) {
        ct.set(t);
    }

    AtomicLong ct;

    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }


}
