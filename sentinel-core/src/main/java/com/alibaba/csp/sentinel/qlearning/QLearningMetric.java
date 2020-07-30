package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import java.util.*;

/**
 * 该类为单例模式，保证了全局变量只能够访问一个公有对象 通过内部类来实现
 * Qlearning算法 和 deepQlearning 相关的算法
 */
public class QLearningMetric {

    private boolean isQLearning = false;

    final int acceptValue = 10;
    final int blockValue = acceptValue; //不相等的话无法判断何时更新Q值，需更改updateInterval和qLearningUpdateManager.isUpdate()

    final int[] actionValues = new int[]{0, acceptValue};

    String[] actionNames = new String[]{"Unchange", "Accept", "Block"};

    private volatile double utilityIncrease;

    private volatile String stateIndex;
    private volatile int action = -1;

    private volatile int statesCount;

    private volatile int actionsCount = actionValues.length;
    // todo: concurrent
    private volatile HashMap<String, double[]> Qtable = new HashMap<>();//把statesMap里的string替换到Qtable当中
//    private volatile HashMap<String, double[]> Qtable = new HashMap<>();//换成Arraylist

    private volatile int maxTrainNum = 50000;
    private volatile boolean isTrain = true;

    private volatile int trainNum = 0; //当前训练到第几次

    private double alpha = 1;//alpha控制了效用方程的qps的参数
    private double beta = 0.2;//控制了效用方程的RT的参数

    private double delta = 0.8;
    private double gamma = 0.2;

    private double tolerance = 0.0001;

    private int rewardValue = 10;
    private int punishValue = -1;

    private int actionInterval = 10;


    private boolean ifCheckCPU;

//    public int getUpdateInterval() {
//        return updateInterval;
//    }

    private int updateInterval = actionInterval - 1;

    private volatile int actionIntervalCount = 0;


    private volatile double currentUtility;
    private volatile double nextUtility;

    private String state;

    public int getOldStateCount() {
        return oldStateCount;
    }

    private volatile int oldStateCount;

    public int getNewStateCount() {
        return newStateCount;
    }

    private volatile int newStateCount;

    public synchronized boolean ifTakeAction() {
//        System.out.println(actionIntervalCount);
        if (this.actionIntervalCount == 0 || this.actionIntervalCount == this.actionInterval) {
            this.actionIntervalCount = 0;
            addActionIntervalCount();
            return true;
        } else if (this.actionIntervalCount < this.actionInterval) {
            addActionIntervalCount();
            return false;
        }
        System.out.println("error in if take action ().");
        return false;
    }

    public synchronized int randomActionValue() {
        int randValue = new Random().nextInt(actionsCount);
        setAction(randValue); //记录执行的动作
        return actionValues[randValue];
    }


    /**
     * 通过Cpu使用率来判断当前所处的状态，返回值为qlearning算法里的当前状态
     * Judging current state by cpu usage，it is current state which is in the Qlearning algorithm。
     *
     * @return
     */
    public synchronized String locateState(double currentCpuUsage, double currentLoad, double totalQps, double Rt, int curThreadNum) {
        int stateC;
        if (ifCheckCPU) {
            stateC = new Double(currentCpuUsage / 0.1).intValue();
        } else {
            stateC = -10;
        }
//        int stateL = new Double(currentLoad / 0.1).intValue();
        int stateQ = new Double(totalQps / 100).intValue();
        int stateR = new Double(Rt / 5).intValue();
        int stateT = new Double(curThreadNum / 50).intValue();

        /**0.1
         * 这里的stateindex是int类型 需要调整 以便对应后面的else情况
         */

        String currentState = stateC + " | " + stateQ + " | " + stateR + " | " + stateT;

        if (!Qtable.containsKey(currentState)) {
            if (!isTrain()) {
                newStateCount++;
//                System.out.print("new State: " + currentState);
            }
            Qtable.put(currentState, new double[actionsCount]);
            //返回current state
        } else if (Qtable.containsKey(currentState)) {//这里需要考虑再加一些其他因果 但是我暂时没想好 等结合了那个新的code再去改好了
            if (!isTrain()) {
                oldStateCount++;
//                System.out.print("old State: " + currentState);
            }
//             stateIndex = Qtable.get(currentState);
        } else {
            System.out.println("Error in get state from Q table.");
        }
        this.state = currentState;
        return currentState;
    }

    public synchronized boolean isUpdate() {
        if (getTrainNum() > 0 && getActionIntervalCount() == updateInterval) {
//            addUpdateIntervalCount();
//            System.out.print(updateIntervalCount);
//            if (getUpdateIntervalCount() < getUpdateInterval()) {
//                return false;
//            } else if (getUpdateIntervalCount() == getUpdateInterval()) {
//                setUpdateIntervalCount(0);
//                return true;
//            }
//            else {
//                setUpdateIntervalCount(0);
//                System.out.println(" error in isUpdate().");
//            }
            return true;
        }
        return false;

    }

    public synchronized void updateQ(double avgRt, double totalQps, int curThreadNum) {

        /**0.2
         * q的数据类型改成了数组 并且调整了qvalue的数据类型
         */
//        System.out.println("Current context: ");
        double q = getQValue(state, action);
//        System.out.print("state: " + state + "  q: " + q);
//        System.out.println("Current context: ");
        //执行action之后的下一个state属于哪个state。
        //locateNextState();

        double cpuUsage = SystemRuleManager.getCurrentCpuUsage();
        double load = SystemRuleManager.getCurrentSystemAvgLoad();
//        int nextState = locateState();
//        System.out.println("Current context: ");
        double maxQ = getmaxQ(cpuUsage, load, avgRt, totalQps, curThreadNum);
//        System.out.println("Current context: ");

//        System.out.println(" getreward() " + getReward());
        //输入变成currentstate 更新单次的对应返回值
        double qValue = q + delta * (getReward() + gamma * maxQ - q);//delta决定了奖励和下一状态的期望值的影响程度 gamma决定了maxQ的影响程度

//        System.out.println(" stateIndex = " + stateIndex + " original q value = " + q + " update q value = " + qValue );
//        System.out.println("  new q: " + qValue);
        setQValue(state, action, qValue);
    }

    public synchronized int getReward() {
        if (getUtilityIncrease() > tolerance) {
            return rewardValue;
        } else if (getUtilityIncrease() < -1 * tolerance) {
            return punishValue;
        } else {
            return 0;
        }
    }

    public synchronized double calculateUtility(double successQPS, double avgRt) {
        double utility = alpha * successQPS - beta * avgRt;
        return utility;
    }

    public synchronized void recordUtilityIncrease() {
        this.utilityIncrease = this.nextUtility - this.currentUtility;
    }

    public synchronized double getmaxQ(double currentCpuUsage, double currentLoad, double totalQps, double Rt, int curThreadNum) {

        if (ifCheckCPU) {
            int stateC = new Double(currentCpuUsage / 0.1).intValue();
        }
        int stateC = -10;
//        int stateL = new Double(currentLoad / 0.1).intValue();
        int stateQ = new Double(totalQps / 100).intValue();
        int stateR = new Double(Rt / 1).intValue();
        int stateT = new Double(curThreadNum / 50).intValue();

//        String nextState = stateC + "#" + stateL + "#" + stateQ + "#" + stateR + "#" + stateT;
        String nextState = stateC + " | " + stateQ + " | " + stateR + " | " + stateT;
        if (!Qtable.containsKey(nextState)) {
            return 0;
        } else {

//            double nextStateIndex = Qtable.get(nextState);//凡是index角标都扔了
            double maxValue = Double.MIN_VALUE;
            for (int i = 0; i < actionsCount; i++) {
                double value = this.Qtable.get(nextState)[i];

                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        }
    }

    public synchronized void showPolicy() {
        String fromState;
        int toAction;
        statesCount = Qtable.size();
        System.out.println("\n ======= Show Policy =======" + statesCount);

        for (Map.Entry entry : Qtable.entrySet()) {
            fromState = (String) entry.getKey();
            toAction = policy(fromState);
            System.out.println("Current State: " + fromState + "       Action: " + actionNames[toAction] + "        Q Value: " + this.Qtable.get(fromState)[toAction]);
        }
    }

    // get policy from state

    /**
     * 0.4
     * 这块地action值不确定有没有用 应该是要返回相应数值的value应该是double数组类型吧？
     *
     * @param state
     */

    public synchronized int policy(String state) {//修改policy方法为相应数值 改成string更合适 intparsing方法不太合适
        double maxValue = Double.MIN_VALUE;
        // default goto self if not found
        int policyGotoAction = 0;

        for (int i = 0; i < actionsCount; i++) {
            int action = i;
            double value = Qtable.get(state)[action];

            if (value > maxValue) {
                maxValue = value;
                policyGotoAction = action;
            }
        }
        setAction(policyGotoAction);
        return policyGotoAction;
    }

    private QLearningMetric() {

    }


    public double getUtilityIncrease() {
        return utilityIncrease;
    }

    public double getQValue(String state, int action) {
//        throw new IllegalArgumentException();
        return this.Qtable.get(state)[action];
    }

    public synchronized void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public synchronized void setQValue(String s, int a, double value) {
//        if(Qtable.size() <= s) Qtable.add(new double[actionsCount]);
        Qtable.get(s)[a] = value;
    }

    public int getTrainNum() {
        return trainNum;
    }

    public synchronized void addTrainNum() {
        this.trainNum++;
    }

//    public int getMaxTrainNum() {
//        return maxTrainNum;
//    }

    public synchronized boolean isTrain() {
        if (this.trainNum <= this.maxTrainNum) {
            this.isTrain = true;
        } else {
            this.isTrain = false;
        }
        return isTrain;
    }

//    public synchronized void setTrain(boolean train) {
//        this.isTrain = train;
//    }

//    public int getUpdateIntervalCount() {
//        return updateIntervalCount;
//    }

//    public synchronized void setUpdateIntervalCount(int updateIntervalCount) {
//        this.updateIntervalCount = updateIntervalCount;
//    }
//
//    public synchronized void addUpdateIntervalCount() {
////        System.out.print(updateIntervalCount + " ------------------- ");
//        this.updateIntervalCount = updateIntervalCount + 1;
////        System.out.println(updateIntervalCount);
//    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public double getAlpha() {
        return alpha;
    }

//    public double getBeta() {
//        return beta;
//    }
//
//    public double getDelta() {
//        return delta;
//    }

    public double getGamma() {
        return gamma;
    }

    public boolean isQLearning() {
        return isQLearning;
    }

    public synchronized void setQLearning(boolean isQLearning) {
        this.isQLearning = isQLearning;
    }


    public HashMap<String, double[]> getQtable() {
        return Qtable;
    }

    public void setMaxTrainNum(int maxTrainNum) {
        this.maxTrainNum = maxTrainNum;
    }

//    public void setStatesMap(HashMap<String, Integer> statesMap) {
//        this.statesMap = statesMap;
//    }
//
//    public int getStateIndex() {
//        return stateIndex;
//    }

    public synchronized void setStateIndex(String stateIndex) {
        this.stateIndex = stateIndex;
    }

    public int getActionIntervalCount() {
        return actionIntervalCount;
    }

//    public void setActionIntervalCount(int actionIntervalCount) {
//        this.actionIntervalCount = actionIntervalCount;
//    }

    public synchronized void addActionIntervalCount() {
        this.actionIntervalCount = this.actionIntervalCount + 1;
    }

    public synchronized void setCurrentUtility(double currentUtility) {
        this.currentUtility = currentUtility;
    }

    public synchronized void setNextUtility(double nextUtility) {
        this.nextUtility = nextUtility;
    }


    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }

    public boolean isIfCheckCPU() {
        return ifCheckCPU;
    }

    public void setIfCheckCPU(boolean ifCheckCPU) {
        this.ifCheckCPU = ifCheckCPU;
    }

}
