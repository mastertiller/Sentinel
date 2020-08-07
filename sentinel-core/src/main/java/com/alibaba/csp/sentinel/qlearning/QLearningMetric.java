package com.alibaba.csp.sentinel.qlearning;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class QLearningMetric {
    /**
     * 注意：原子类变量不能直接用！如this.bi，应该用getBi()！
     */
    ///////////////////////需要检查是否上锁的变量/////////////////////////
    public boolean ifCheckCPU;
    public final int maxTrainNum = 5000000;

    final int[] actionValues = new int[]{0, 1};
    private volatile int actionsCount = actionValues.length;

    private volatile ConcurrentHashMap<String, double[]> Qtable = new ConcurrentHashMap<>();


    private final double alpha = 0.1;//alpha控制了效用方程的qps的参数
    private final double beta = 0.01;//控制了效用方程的RT的参数

    private final double delta = 0.8;
    private final double gamma = 0.05;

    private final double tolerance = 0.1;

    private int rewardValue = 10;
    private int punishValue = -1;
    private final double CpuInterval = 0.1;
    private final int QpsInterval = 200;
    private final int RtInterval = 10;
    private final int ThreadInterval = 2;


    ///////////////////////计时与计数器///////////////////
    public long getCt() {
        return ct.get();
    }

    public void setCt(long t) {
        ct.set(t);
    }

    public int getCn() {
        return cn.get();
    }

    public void addCn() {
        cn.incrementAndGet();
    }

    public void resetCn(){
        cn.set(0);
    }
    private AtomicLong ct = new AtomicLong(0);
    private AtomicInteger cn = new AtomicInteger(0);

    public int getBi() {
        return bi.get();
    }

    public int addBi() {
        return bi.incrementAndGet();
    }
    public void setBi(int i){
        bi.set(i);
    }

    private AtomicInteger bi = new AtomicInteger(0);

    /////////////////////////保存QInfo////////////////////////////////

    public ConcurrentHashMap<Integer,QInfo > getHm() {
        return hm;
    }

    public void putHm(int i,QInfo qInfo) {
        System.out.println("存下： " + i + "   " + qInfo.getState() + "   " + qInfo.getAction() + "   " + qInfo.getUtility() );
        this.hm.put(i,qInfo);
    }

    public QInfo pushQInfo() {
        if(!getHm().containsKey(getBi())){
            System.out.println("   pushQInfo: 不存在");
        }
        QInfo qInfo = getHm().get(getBi());
//        this.hm.remove(this.bi);
        return qInfo;
    }

    private ConcurrentHashMap<Integer, QInfo> hm = new ConcurrentHashMap<>();

    ////////////////////////////action//////////////////////////

    public int getAction(){
        return this.action.get();
    }
    public void setAction(int a) {
        this.action.set(a);
    }

    private AtomicInteger action = new AtomicInteger(1);

    /////////////////////////////方法//////////////////////////////////////

    /**
     * 通过Cpu使用率来判断当前所处的状态，返回值为qlearning算法里的当前状态
     * Judging current state by cpu usage，it is current state which is in the Qlearning algorithm。
     *
     * @return
     */
    public String locateState(double currentCpuUsage, double totalQps, double Rt, int curThreadNum) {
        int stateC;
        if (ifCheckCPU) {
            stateC = new Double(currentCpuUsage / CpuInterval).intValue();
        } else {
            stateC = -10;
        }
//        int stateL = new Double(currentLoad / 0.1).intValue();
        int stateQ = new Double(totalQps / QpsInterval).intValue();
        int stateR = new Double(Rt / RtInterval).intValue();
        int stateT = new Double(curThreadNum / ThreadInterval).intValue();

        String currentState = stateC + "#" + stateQ + "#" + stateR + "#" + stateT;

        if (!Qtable.containsKey(currentState)) {
            //测试用
//            if (!isTrain()) {
//                newStateCount++;
//                System.out.print("new State: " + currentState);
//            }
//            System.out.println("training - new State: " + currentState);
            Qtable.put(currentState, new double[actionsCount]);
            //返回current state
        } else if (Qtable.containsKey(currentState)) {
            //测试用
//            if (!isTrain()) {
//                oldStateCount++;
//                System.out.print("old State: " + currentState);
//            }
//            System.out.println("training - old State: " + currentState);
        } else {
            System.out.println("Error in get state from Q table.");
        }
        return currentState;
    }

    public double calculateUtility(double successQPS, double avgRt) {
        return alpha * successQPS - beta * avgRt;
    }

    public int getReward(double u,double nextU) {
        double UtilityIncrease = nextU - u;
        if ( UtilityIncrease> tolerance) {
            return rewardValue;
        } else if (UtilityIncrease < -1 * tolerance) {
            return punishValue;
        } else {
            return 0;
        }
    }

    public double getQValue(String state, int action) {
        return this.Qtable.get(state)[action];
    }

    public void setQValue(String s, int a, double value) {
        Qtable.get(s)[a] = value;
    }

    public double getMaxQ(double currentCpuUsage, double totalQps, double Rt, int curThreadNum) {
        int stateC;
        if (ifCheckCPU) {
            stateC = new Double(currentCpuUsage / CpuInterval).intValue();
        } else {
            stateC = -10;
        }
//        int stateL = new Double(currentLoad / 0.1).intValue();
        int stateQ = new Double(totalQps / QpsInterval).intValue();
        int stateR = new Double(Rt / RtInterval).intValue();
        int stateT = new Double(curThreadNum / ThreadInterval).intValue();

//        String nextState = stateC + "#" + stateL + "#" + stateQ + "#" + stateR + "#" + stateT;
        String nextState = stateC + "#" + stateQ + "#" + stateR + "#" + stateT;
        if (!Qtable.containsKey(nextState)) {
            return 0;
        } else {
            double maxValue = Double.MIN_VALUE;
            double value;
            for (int i = 0; i < actionsCount; i++) {
                value = this.Qtable.get(nextState)[i];
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        }
    }

    public int policy(String state) {
        double maxValue = Double.MIN_VALUE;
        double value;
        int policyGotoAction = 0;
        for (int a = 0; a < actionsCount; a++) {
            value = Qtable.get(state)[a];
            if (value >= maxValue) {
                maxValue = value;
                policyGotoAction = a;
            }
        }
        return policyGotoAction;
    }

    public synchronized int getRandomAction() {
        return new Random().nextInt(actionsCount);
    }

    public synchronized boolean isTrain() {
        if (this.getBi() <= this.maxTrainNum) {
            return true;
        }
        return false;
    }

    public double updateQ(double q, int r, double maxQ) {
        return q + delta * (r + gamma * maxQ - q);
    }

    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }



}
