package com.alibaba.csp.sentinel.qlearning.demo;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.qlearning.QLearningMetric;
import com.alibaba.csp.sentinel.qlearning.qtable.QTable;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UserPeakDemo {
    private static final String KEY = "abc";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static volatile boolean stop = false;
    private static volatile boolean stop1 = false;
    private static volatile boolean stop2 = false;
    private static volatile boolean start2 = false;
    private static volatile boolean flag = false;
    private static AtomicInteger Interval2 = new AtomicInteger(1000);
    private static final int threadCount = 100;
    private static int seconds = 60 + 40;

    static QLearningMetric qLearningMetric = QLearningMetric.getInstance();
    static QTable qTableTrain = new QTable();

    private static boolean isQLearning = false;

    //set a switch， when it is true it will employ Qlearnig algorithm. If not it will use BBR algorithm.
    private static String qTablePath = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/qlearning/demo/" + Thread.currentThread().getStackTrace()[1].getClassName() + "-QTable.txt";

    //是否考虑CPU这个state
    private static boolean ifCheckCPU = false;

    public static void main(String[] args) throws Exception {

        QLearningMetric qLearningMetric = QLearningMetric.getInstance();
        qLearningMetric.setLearning(isQLearning);
        qLearningMetric.setIfCheckCPU(ifCheckCPU);

        //是否更新
        qLearningMetric.setQtable(qTableTrain.read(qTablePath));

        Entry entry = null;
        try {
            entry = SphU.entry(KEY);
        } catch (Exception e) {
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

        tick();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new StableTask());
            t.setName("stable-task");
            t.start();
        }
        Thread.sleep(20000);

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask1());
            t.setName("sentinel-run-task1");
            t.start();
        }
        Thread.sleep(40000);
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask2());
            t.setName("sentinel-run-task2");
            t.start();
        }
    }

    static class StableTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    // token acquired, means pass
                    pass.incrementAndGet();
                } catch (BlockException e1) {
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }
                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(500));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }
    static class RunTask1 implements Runnable {
        @Override
        public void run() {
            while (!stop1) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }
                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(5));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    static class RunTask2 implements Runnable {
        @Override
        public void run() {
            while (!stop2) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    pass.addAndGet(1);

                } catch (BlockException e1) {
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }
                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(2));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    static class TimerTask implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("begin to statistic!!!");

            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                System.out.println(seconds + " send qps is: " + oneSecondTotal);
                System.out.print(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                        + ", pass:" + oneSecondPass
                        + ", block:" + oneSecondBlock);
                if (qLearningMetric.isTrain()) {
                    System.out.println(" ------now is training------ ");
                } else {
                    System.out.println();
                }
                if(Interval2.get() >= 49) {
                    Interval2.set(Interval2.get() - 39);
                }
                if(seconds <= 70) {
                    stop1 = true;
                }
                if(seconds <= 20) {
                    stop2 = true;
                }
                if (seconds-- <= 0) {
                    stop = true;
                }

            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total:" + total.get() + ", pass:" + pass.get()
                    + ", block:" + block.get());

//            if (qLearningMetric.isQLearning()){
//            System.out.println(" new state number: " + qLearningMetric.getNewStateCount() + "   old state number: " + qLearningMetric.getOldStateCount());
            qLearningMetric.showPolicy();
            ConcurrentHashMap<String, double[]> qtable = qLearningMetric.getQtable();

            qTableTrain.save(qtable,qTablePath);
//                for ( int i = 0; i < qtable.size(); i ++){
//                    System.out.println("state: " + i + "  block: " + qtable.get(i)[0] + "  accept: " + qtable.get(i)[1]);
//                }
//            }
            System.exit(0);
        }
    }
}
