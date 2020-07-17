package com.alibaba.csp.sentinel.qlearning.Qtable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class QtableTest {
//    static ArrayList<Double> ans = new ArrayList<>();
//    static HashMap<String, Double[]> map = new HashMap<>();

//    {2.2, 3.4, ...}
//    {[2.2, 3.4],}


    //      key        value
//    172*8/*/ 1.2 2.0
//    2#3#1#4.4#5.9 2.0 2.0
    static HashMap<String, double[]> Qtable = new HashMap<>();

    public static void main (String[] args) throws IOException {

        Qtable.put("2#3#1#4.4#5.9", new double[]{1.2, 2.0});
        Qtable.put("11w242", new double[]{1.5, 3.8});
//        Qtable.put("172*8/*/",new double[]{2.0,2.0});//如何做一个动态的写入信息呢？

        save();
//        readFile();
//        for (Map.Entry entry : Qtable.entrySet()) {
//            String key = (String) entry.getKey();
//            double[] value = (double[]) entry.getValue();
//            String line = key + " " + value[0] + " " + value[1];
//            System.out.println(line);
//        }


    }

//    static ArrayList<Double> ans;
//    static HashMap<String, Integer> map;



    private static void save () {

//        map.put("185+54",186.0);
//         map = [<{"2#3#1#4.4#5.9", 100> <{" xs#sdfds", 2}]
//         txt = 2#3#1#4.4#5.9 1.1 2.3 100
//        数组保存到文件中 txt  空格分隔
        try ( //改成相对路径
              PrintStream output = new PrintStream(new File("QtableData/test.txt"));) {

            for (Map.Entry entry : Qtable.entrySet()) {
                String key = (String) entry.getKey();
                double[] value = (double[]) entry.getValue();
                String line = key + " " + value[0] + " " + value[1];
                output.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
//}

//    public HashMap<String, Double> getMap () {
//        return map;
//    }
//
//    public ArrayList<Double> getAns () {
//        return ans;
//    }


        public static void readFile () throws IOException {
            String pathname = "QtableData/test.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
            //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
            // {"2#3#1#4.4#5.9", 4, 3}
//            HashMap<String, Double[]> Qtable = new HashMap<>();
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
                    Qtable.put(lineList[0], temp);

//                System.out.println();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


//        创建集合对象
//        ArrayList<String> array = new ArrayList<>();
//        ArrayList<Double> Qtable = new ArrayList<>();//元素一个double数
//
//        //创建输入缓冲流对象
//        BufferedReader br = new BufferedReader(new FileReader("QtableData/test.txt"));
//        String line;
//        while ((line = br.readLine()) != null) {
//            array.add(line);
//
//        }
//
//
//        //释放资源
//        br.close();
//
//        for (int x = 0; x < array.size(); x++) {
//            System.out.println(array.get(x));
//        }






