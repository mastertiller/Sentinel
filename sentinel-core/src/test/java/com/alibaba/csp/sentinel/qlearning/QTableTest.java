package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.qlearning.qtable.QTable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QTableTest {

    static String filePath = "sentinel-core/src/test/java/com/alibaba/csp/sentinel/qlearning/qTableTest.txt";

    public static void main (String[] args) throws IOException {
        HashMap<String, double[]> qTableSave = new HashMap<>();
        qTableSave.put("2#3#1#4.4#5.9", new double[]{1.2, 2.0});
        qTableSave.put("11w242", new double[]{1.5, 3.8});

        //后期做实时批量写入
        QTable qTable = new QTable();
        qTable.save(qTableSave,filePath);

        HashMap<String, double[]> qTableRead = qTable.read(filePath);

        for (Map.Entry entry : qTableRead.entrySet()) {
            String key = (String) entry.getKey();
            double[] value = (double[]) entry.getValue();
            String line = key + " " + value[0] + " " + value[1];
            System.out.println(line);
        }

    }
}
