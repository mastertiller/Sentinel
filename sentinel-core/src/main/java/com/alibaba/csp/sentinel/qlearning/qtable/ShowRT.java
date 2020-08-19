package com.alibaba.csp.sentinel.qlearning.qtable;

import com.alibaba.csp.sentinel.qlearning.QLearningMetric;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ShowRT {
    static QLearningMetric qLearningMetric;



    public static void main(String[] args) {
        try {
            qLearningMetric = new QLearningMetric().getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConcurrentHashMap rtMap = qLearningMetric.getRtMap();
        for()
    }
}
