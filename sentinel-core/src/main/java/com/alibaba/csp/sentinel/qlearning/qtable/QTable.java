package com.alibaba.csp.sentinel.qlearning.qtable;

import com.sun.org.apache.xerces.internal.xs.StringList;
import javafx.scene.control.Labeled;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class QTable {}
//    public static void main (String[] args) throws IOException {
//
//        while (true) {
//            double[][] qtable = {{5, 4, 3}, {7, 8, 9}, {6.5, 2}, {4, 8}};
//            save(qtable);
////            readFile();
////        }
////
////    }
////

//    private static void save (double[][] Qtable) {
//        //数组保存到文件中 txt  空格分隔
//        try ( //改成相对路径
//              PrintStream output = new PrintStream(new File("QtableData/test.txt"));) {
//            for (int i = 0; i < Qtable.length; i++) {
//                String s = "";
//                for (int j = 0; j < Qtable[i].length; j++) {
//                    s += " " + Qtable[i][j] + "  ";
//                }
//                output.println(s);
//
//            }
//            //关闭output
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void readFile () throws IOException {
//        String pathname = "QtableData/test.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
//        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
//        double d [][];
//        try (FileReader reader = new FileReader(pathname);
//             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
//        ) {
//            String line;
//
//            line = br.readLine();//读取数据
//            double[] temp = new double[2] {line.split(" ");
//            while ((line = br.readLine()) != null) {
//                // 一次读入一行数据
//                System.out.println(line);
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
////        String s = "1 2 4 5 6 7 8 9";
////        double[][] d;
////        String[] sFirst = s.split(" ");
////        d = new double[sFirst.length][];
////        for (int i = 0; i < sFirst.length; i++) {
////            String[] sSecond = sFirst[i].split(" ");
////            d[i] = new double[sSecond.length];
////            for (int j = 0; j < sSecond.length; j++) {
////                d[i][j] = Double.parseDouble(sSecond[j]);
////            }
////        }
////
////        for (int i = 0; i < d.length; i++) {
////            for (int j = 0; j < d[i].length; j++) {
////                System.out.print(d[i][j] + " ");
////            }
////            System.out.println();
////
////
////        }
//
//    }
//
////        创建集合对象
////        ArrayList<String> array = new ArrayList<>();
////        ArrayList<Double> Qtable = new ArrayList<>();//元素一个double数
////
////        //创建输入缓冲流对象
////        BufferedReader br = new BufferedReader(new FileReader("QtableData/test.txt"));
////        String line;
////        while ((line = br.readLine()) != null) {
////            array.add(line);
////
////        }
////
////
////        //释放资源
////        br.close();
////
////        for (int x = 0; x < array.size(); x++) {
////            System.out.println(array.get(x));
////        }
//
//
//
//    }

