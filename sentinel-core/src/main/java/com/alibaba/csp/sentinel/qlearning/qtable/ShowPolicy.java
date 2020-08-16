package com.alibaba.csp.sentinel.qlearning.qtable;

import com.alibaba.csp.sentinel.qlearning.QLearningMetric;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShowPolicy {
    private static String qTablePath = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/qlearning/qtable/QTable-UserPeak.txt";
    static QLearningMetric qLearningMetric = new QLearningMetric().getInstance();

    public static void main(String[] args) throws IOException {
        QTable qTable = new QTable();
        ConcurrentHashMap<String, double[]> qTableRead = qTable.read(qTablePath);
        qLearningMetric.setQtable(qTableRead);
        qLearningMetric.showPolicy();
    }
}
