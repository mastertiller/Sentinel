package com.alibaba.csp.sentinel.qlearning.Qtable;

import java.io.*;

public class Qtable {
    public static void main (String[] args) throws IOException {

        while(true) {
            double[][] qtable = {{5, 4, 3}, {7, 8, 9}, {6.5, 2}, {4, 8}};
            save(qtable);
            readFile();
        }

    }

    private static void save (double[][] Qtable) {
        //数组保存到文件中 txt  空格分隔
        try ( //改成相对路径
              PrintStream output = new PrintStream(new File("QtableData/test.txt"));) {
            for (int i = 0; i < Qtable.length; i++) {
                String s = "";
                for (int j = 0; j < Qtable[i].length; j++) {
                    s += " " + Qtable[i][j] + "  ";
                }
                output.println(s);

            }
            //关闭output
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void readFile() {
        String pathname = "QtableData/test.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
