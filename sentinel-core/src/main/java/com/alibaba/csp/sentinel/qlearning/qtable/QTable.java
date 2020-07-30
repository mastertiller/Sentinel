package com.alibaba.csp.sentinel.qlearning.qtable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QTable{


    public synchronized static void save(ConcurrentHashMap<String, double[]> qTable, String filePath) {
//        map.put("185+54",186.0);
//         map = [<{"2#3#1#4.4#5.9", 100> <{" xs#sdfds", 2}]
//         txt = 2#3#1#4.4#5.9 1.1 2.3 100
//        数组保存到文件中 txt  空格分隔

        try ( //改成相对路径
              PrintStream output = new PrintStream(new File(filePath));) {

            for (Map.Entry entry : qTable.entrySet()) {
                String key = (String) entry.getKey();
                double[] value = (double[]) entry.getValue();
                String line = key + " " + value[0] + " " + value[1];
                output.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static ConcurrentHashMap<String, double[]> read(String pathname) throws IOException {
        ConcurrentHashMap<String, double[]> qTable = new ConcurrentHashMap<>();
//        String pathname = "QTableData/test.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        // {"2#3#1#4.4#5.9", 4, 3}
//            HashMap<String, Double[]> Qtable = new HashMap<>();

        File file = new File(pathname);
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println(" no q table exists.");
                return qTable;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(pathname);
                 BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
            ) {
////             line = br.readLine().split(" ");//读取数据
////            String[] temp = new String[0];
                String line;

                while ((line = br.readLine()) != null) {
                    // 一次读入一行数据
                    String[] lineList = line.split(" ");
                    double[] temp = new double[]{Double.valueOf(lineList[1]),Double.valueOf(lineList[2])};
                    qTable.put(lineList[0], temp);

//                System.out.println();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return qTable;
    }

    public static void recordLog(String log,String filePath){
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(filePath, true);
            writer.write(log);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}