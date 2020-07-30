package com.alibaba.csp.sentinel.qlearning.demo;

import java.util.HashMap;
import java.util.Map;

public class hashmapdemo {
    static HashMap<String, double[]> qTable = new HashMap<>();
    private static String state;
    public static void main(String[] args) {
        state = "1 | 2 | 3 | 3 | 4";
        qTable.put(state, new double[2]);
        qTable.get(state)[1] = 10;

        for (Map.Entry entry : qTable.entrySet()) {
            String key = (String) entry.getKey();
            double[] value = (double[]) entry.getValue();
            String line = key + " " + value[0] + " " + value[1];
            System.out.println(line);
        }
    }
}