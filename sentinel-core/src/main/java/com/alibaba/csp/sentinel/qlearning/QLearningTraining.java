package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.qlearning.demo.QLearningDemo;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//不同的流量场景demo继承这个类，直接调用这个类的方法
//首先需要loadQtable
//然后在训练过程中更新Qtable
//训练完成后调用saveQtable
//这些方法都可以直接放到qLearningMetric中
public class QLearningTraining {

    static QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger activeThread = new AtomicInteger();

    private static ArrayList<Double> avgRTArray = new ArrayList<Double>();
    private static ArrayList<Double> qpsArray = new ArrayList<Double>();

    private static volatile boolean stop = false;
    private static final int threadCount = 100;

    private static int seconds = 20 + 60;

    private static boolean isQLearning = true;
    private static int maxTrainNum = 1000000;

    //设置训练的超参数，需要调参:
    private static double alpha = 1;//alpha控制了效用方程的qps的参数
    private static double beta = 0.02;//控制了效用方程的RT的参数

    private static double delta = 1;
    private static double gamma = 1;

    private static double tolerance = 0.01;


    public static void main(String[] args) throws Exception {

        QLearningMetric qLearningMetric = QLearningMetric.getInstance();
        qLearningMetric.setQLearning(isQLearning);
        qLearningMetric.setMaxTrainNum(maxTrainNum);
        qLearningMetric.setAlpha(alpha);
        qLearningMetric.setBeta(beta);
        qLearningMetric.setDelta(delta);
        qLearningMetric.setGamma(gamma);
        qLearningMetric.setTolerance(tolerance);

        //读取Qtable方法
        loadQtable();

        tick();
        initRule();

        //存储更新后的Qtable
        saveQtable();

        //模拟发送请求：

    }


    private static void tick() {
        Thread timer = new Thread(new QLearningDemo.TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    private static void initRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();

    }

    static class TimerTask implements Runnable {
        @Override
        public void run() {
            System.out.println("begin to statistic!!!");
            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                double avgRt = Constants.ENTRY_NODE.avgRt();
                double successQps = Constants.ENTRY_NODE.successQps();

                avgRTArray.add(avgRt);
                qpsArray.add(successQps);

                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                System.out.print(seconds + ", " + TimeUtil.currentTimeMillis() + ", total:"
                        + oneSecondTotal + ", pass:"
                        + oneSecondPass + ", block:" + oneSecondBlock);
                if (qLearningMetric.isQLearning() && qLearningMetric.isTrain()) {
                    System.out.println(" ------now is training------ ");
                } else {
                    System.out.println();
                }
                if (seconds-- <= 0) {
                    stop = true;
                }
            }

            printArray(avgRTArray, "Average RT");
            printArray(qpsArray, "Success QPS");
            if (qLearningMetric.isQLearning()){
                qLearningMetric.showPolicy();
            }
            System.exit(0);
        }
    }

    private static void printArray(List<Double> array, String name) {
        System.out.println(name + " result:");
        System.out.print("[");
        for (double val : array) {
            System.out.print(val + ", ");
        }
        System.out.print("]");
        System.out.println();
    }

    public static void loadQtable(){
//        qLearningMetric.setQtable();

    }

    public static void saveQtable(){
        HashMap<String, double[]> qTable = qLearningMetric.getQtable();
        //存入新的
    }


}
