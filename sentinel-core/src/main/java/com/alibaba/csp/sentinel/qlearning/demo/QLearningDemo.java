package com.alibaba.csp.sentinel.qlearning.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.qlearning.QLearningMetric;
import com.alibaba.csp.sentinel.qlearning.qtable.QTable;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

public class QLearningDemo {

    static QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    private static final String KEY = "abc";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger activeThread = new AtomicInteger();

    private static ArrayList<Double> avgRTArray = new ArrayList<Double>();
    private static ArrayList<Double> qpsArray = new ArrayList<Double>();

    private static volatile boolean stop = false;
    private static final int threadCount = 100;

    private static int seconds = 20 + 60;
    private static volatile int methodBRunningTime = 1000;

    private static boolean isQLearning = true;
    //如果考虑CPU这个state，为true
    private static boolean ifCheckCPU = false;

    private static String qTablePath = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/qlearning/demo/" + QLearningDemo.class.getSimpleName() + "-QTable.txt";

    //set a switch， when it is true it will employ Qlearnig algorithm. If not it will use BBR algorithm.

    public static void main(String[] args) throws Exception {

        QLearningMetric qLearningMetric = QLearningMetric.getInstance();
        qLearningMetric.setQLearning(isQLearning);
        qLearningMetric.setIfCheckCPU(ifCheckCPU);

        System.out.println(
                "MethodA will call methodB. After running for a while, methodB becomes fast, "
                        + "which make methodA also become fast ");

        tick();
        initFlowThreadRule();

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Entry methodA = null;
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                            methodA = SphU.entry("methodA",EntryType.IN);
                            activeThread.incrementAndGet();
                            Entry methodB = SphU.entry("methodB");
                            TimeUnit.MILLISECONDS.sleep(methodBRunningTime);
                            methodB.exit();
                            pass.addAndGet(1);
                        } catch (BlockException e1) {
                            block.incrementAndGet();
                        } catch (Exception e2) {
                            // biz exception
                        } finally {
                            total.incrementAndGet();
                            if (methodA != null) {
                                methodA.exit();
                                activeThread.decrementAndGet();
                            }
                        }
                    }
                }
            });
            entryThread.setName("working thread");
            entryThread.start();
        }

        System.out.println("===== begin to do flow control");
        System.out.println("only 20 requests per second can pass");

    }

    private static void initFlowThreadRule() {
//        List<FlowRule> rules = new ArrayList<FlowRule>();
//
//        FlowRule rule1 = new FlowRule();
//        rule1.setResource(KEY);
//        // set limit qps to 20
//        rule1.setCount(1200);
//        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule1.setLimitApp("default");
//        rules.add(rule1);
//        FlowRule rule2 = new FlowRule();
//        rule2.setResource(KEY);
//        // set limit qps to 20
//        rule2.setCount(3000);
//        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule2.setLimitApp("default");
//        rules.add(rule2);
//        qLearningMetric.setRules(rules);
//        FlowRuleManager.loadRules(rules);

//        FlowRule rule3 = new FlowRule();
//        rule3.setResource("methodA");
//        // set limit concurrent thread for 'methodA' to 20
//        rule3.setCount(20);
//        rule3.setGrade(RuleConstant.FLOW_GRADE_THREAD);
//        rule3.setLimitApp("default");
//
//        rules.add(rule3);
//        FlowRuleManager.loadRules(rules);
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    public static class TimerTask implements Runnable {

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

                double avgRt = Constants.ENTRY_NODE.avgRt();
                double successQps = Constants.ENTRY_NODE.successQps();

                System.out.println(seconds + " total qps is: " + oneSecondTotal);
                System.out.print(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                        + ", pass:" + oneSecondPass
                        + ", block:" + oneSecondBlock
                        + " activeThread:" + activeThread.get());

                System.out.print(" TEST ----- " + successQps);
                avgRTArray.add(avgRt);
                qpsArray.add(successQps);

                if (qLearningMetric.isQLearning() && qLearningMetric.isTrain()) {
                    System.out.println(" ------now is training------ ");
                } else {
                    System.out.println();
                }

                if (seconds-- <= 0) {
                    stop = true;
                }
                if (seconds == 20) {
                    System.out.println("method B is running much faster; more requests are allowed to pass");
                    methodBRunningTime = 10;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total:" + total.get() + ", pass:" + pass.get()
                    + ", block:" + block.get());


            printArray(avgRTArray, "Average RT");
            printArray(qpsArray, "Success QPS");
            if (qLearningMetric.isQLearning()){
                System.out.println(" new state number: " + qLearningMetric.getNewStateCount() + "   old state number: " + qLearningMetric.getOldStateCount());
                qLearningMetric.showPolicy();
                ConcurrentHashMap<String, double[]> qtable = qLearningMetric.getQtable();
                QTable qTableTrain = new QTable();
                qTableTrain.save(qtable,qTablePath);
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

//    static class RunTask implements Runnable {
//        @Override
//        public void run() {
//            while (!stop) {
//                Entry entry = null;
//
//                try {
//                    entry = SphU.entry(KEY);
//                    // token acquired, means pass
//                    pass.addAndGet(1);
//                } catch (BlockException e1) {
//                    block.incrementAndGet();
//                } catch (Exception e2) {
//                    // biz exception
//                } finally {
//                    total.incrementAndGet();
//                    if (entry != null) {
//                        entry.exit();
//                    }
//                }
//
//                Random random2 = new Random();
//                try {
//                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(50));
//                } catch (InterruptedException e) {
//                    // ignore
//                }
//            }
//        }
//    }
}

