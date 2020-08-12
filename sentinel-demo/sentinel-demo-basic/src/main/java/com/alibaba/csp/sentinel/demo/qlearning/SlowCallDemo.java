package com.alibaba.csp.sentinel.demo.qlearning;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.qlearning.QLearningMetric;
import com.alibaba.csp.sentinel.qlearning.qtable.QTable;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SlowCallDemo {
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger activeThread = new AtomicInteger();

    private static volatile boolean stop = false;
    private static final int threadCount = 100;

    private static int seconds = 60 + 40;
    private static volatile int methodBRunningTime = 2000;

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

        System.out.println(
                "MethodA will call methodB. After running for a while, methodB becomes fast, "
                        + "which make methodA also become fast ");
        tick();
//        initFlowRule();

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Entry methodA = null;
                        try {
                            TimeUnit.MILLISECONDS.sleep(5);
                            methodA = SphU.entry("methodA");
                            activeThread.incrementAndGet();
                            Entry methodB = SphU.entry("methodB");
                            TimeUnit.MILLISECONDS.sleep(methodBRunningTime);
                            methodB.exit();
                            pass.addAndGet(1);
                        } catch (BlockException e1) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(methodBRunningTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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
    }

    private static void initFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource("methodA");
        // set limit concurrent thread for 'methodA' to 20
        rule1.setCount(20);
        rule1.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule1.setLimitApp("default");

        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
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

                System.out.println(seconds + " total qps is: " + oneSecondTotal);
                System.out.print(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                        + ", pass:" + oneSecondPass
                        + ", block:" + oneSecondBlock
                        + " activeThread:" + activeThread.get());
                if (qLearningMetric.isTrain()) {
                    System.out.println(" ------now is training------ ");
                } else {
                    System.out.println();
                }
                if (seconds-- <= 0) {
                    stop = true;
                }
                if (seconds == 40) {
                    System.out.println("method B is running much faster; more requests are allowed to pass");
                    methodBRunningTime = 20;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total:" + total.get() + ", pass:" + pass.get()
                    + ", block:" + block.get());

            qLearningMetric.showPolicy();
            ConcurrentHashMap<String, double[]> qtable = qLearningMetric.getQtable();

            qTableTrain.save(qtable,qTablePath);

            System.exit(0);
        }
    }
}
