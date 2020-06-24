package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
/*


 */

public class QLearningTrainDemo {

    static QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    private static final String KEY = "abc";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static ArrayList<Double> avgRTArray = new ArrayList<Double>();
    private static ArrayList<Double> qpsArray = new ArrayList<Double>();

    private static volatile boolean stop = false;
    private static final int threadCount = 100;//当前线程数

    private static int seconds = 40;//整个程序运行时间

    private static boolean isQLearning = true;
    //set a switch， when it is true it will employ Qlearnig algorithm. If not it will use BBR algorithm.

    public static void main(String[] args) throws Exception {

        QLearningMetric qLearningMetric = QLearningMetric.getInstance();
        qLearningMetric.setQLearning(isQLearning);

        tick();
        initSystemRule();

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Entry entry = null;
                        try {
                            entry = SphU.entry("methodA", EntryType.IN);
                            pass.incrementAndGet();
                            try {
                                TimeUnit.MILLISECONDS.sleep(10);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        } catch (BlockException e1) {
                            block.incrementAndGet();
                            try {
                                TimeUnit.MILLISECONDS.sleep(10);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        } catch (Exception e2) {
                            // biz exception
                        } finally {
                            total.incrementAndGet();
                            if (entry != null) {
                                entry.exit();
                            }
                        }
                    }
                }

            });
            entryThread.setName("working-thread");
            entryThread.start();
        }

    }

    private static void initSystemRule() {
//        List<SystemRule> rules = new ArrayList<SystemRule>();
//        SystemRule rule = new SystemRule();
////        // max load is 3
////        rule.setHighestSystemLoad(3.0);
//        //max cpu usage is 60%
//        rule.setHighestCpuUsage(1);
////        //max avg rt of all request is 10 ms
////        rule.setAvgRt(20);
////        //max total qps is 20
////        rule.setQps(500);
////        //max parallel working thread is 10
////        rule.setMaxThread(100);
//
//        rules.add(rule);
//        SystemRuleManager.loadRules(Collections.singletonList(rule));

        List<FlowRule> rulesN = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource("methodA");
        // set limit qps to 20
        rule1.setCount(3000);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rulesN.add(rule1);
        FlowRule rule2 = new FlowRule();
        rule2.setResource("methodA");
        // set limit qps to 20
        rule2.setCount(1500);
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setLimitApp("default");
        rulesN.add(rule2);
        qLearningMetric.setRules(rulesN);
        FlowRuleManager.loadRules(rulesN);
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
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
}

