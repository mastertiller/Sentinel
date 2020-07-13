package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import java.util.*;

/**
 * 该类为单例模式，保证了全局变量只能够访问一个公有对象 通过内部类来实现
 */
public class QLearningMetric {


    private boolean isQLearning;

    final int acceptValue = 10;
    final int blockValue = acceptValue; //不相等的话无法判断何时更新Q值，需更改updateInterval和qLearningUpdateManager.isUpdate()

//    public void setStateNum(int stateNum) {//设置状态
//        this.stateNum = stateNum;
//        for (int i = 0; i <= stateNum; i++){
//            this.states[i] = i;
//        }
//    }
//    private int stateNum;

//    private Set<Integer> cpuStates = new HashSet<Integer>();
//    private Set<Integer> loadStates = new HashSet<Integer>();
//    private Set<Integer> qpsStates = new HashSet<Integer>();
//    private Set<Integer> rtStates = new HashSet<Integer>();
//    private Set<Integer> threadStates = new HashSet<Integer>();

    //    final int[] states = new int[]{stateA, stateB, stateC, stateD, stateE};
//    private volatile ArrayList<Integer> states = new ArrayList<Integer>();

    final int[] actionValues = new int[]{0, acceptValue};

    String[] stateNames = new String[]{"CPU Usage: UnGet", "CPU Usage: (0%, 25%)", "CPU Usage: (25%, 50%)", "CPU Usage: (50%, 75%)", "CPU Usage: (75%, 100%)"};
    String[] actionNames = new String[]{"Unchange", "Accept", "Block"};

    private volatile double utilityIncrease;

    public HashMap<String, Integer> getStatesMap() {
        return statesMap;
    }

    public void setStatesMap(HashMap<String, Integer> statesMap) {
        this.statesMap = statesMap;
    }

    //    private volatile int[] state = new int[5];
    private volatile HashMap<String,Integer> statesMap = new HashMap<>();

    public int getStateIndex() {
        return stateIndex;
    }

    public synchronized void setStateIndex(int stateIndex) {
        this.stateIndex = stateIndex;
    }

    private volatile int stateIndex;
    private volatile int action;

    private volatile int statesCount;


    public synchronized int randomActionValue() {
        int randValue = new Random().nextInt(actionsCount);
        setAction(randValue); //记录执行的动作
        return actionValues[randValue];
    }

    private volatile int actionsCount = actionValues.length;


//    public void setQtable(ArrayList<double[]> qtable) {
//        Qtable = qtable;
//    }

    public ArrayList<double[]> getQtable() {
        return Qtable;
    }

    //    private volatile double[][] Qtable = new double[statesCount][actionsCount];
   private volatile ArrayList<double[]> Qtable = new ArrayList<double[]>();
//    private Set<Integer> cpuStates = new HashSet<Integer>();

    public void setMaxTrainNum(int maxTrainNum) {
        this.maxTrainNum = maxTrainNum;
    }

    private volatile int maxTrainNum = 10000;
    private volatile boolean isTrain = true;

    private volatile int trainNum = 0; //当前训练到第几次

    private double alpha = 1;//alpha控制了效用方程的qps的参数
    private double beta = 0.2;//控制了效用方程的RT的参数

    private double delta = 1;
    private double gamma = 1;

    private double tolerance = 0.0001;

    private int rewardValue = 10;
    private int punishValue = -1;

    private int actionInterval = 10;

    public int getUpdateInterval() {
        return updateInterval;
    }

    private int updateInterval = actionInterval - 1;
    private int updateIntervalCount = 0;


//    private int ruleQPSCount = 10;

    public List<FlowRule> getRules() {
        return rules;
    }

    public void setRules(List<FlowRule> rules) {
        this.rules = rules;
    }

    private List<FlowRule> rules = new ArrayList<FlowRule>();

    public int getActionIntervalCount() {
        return actionIntervalCount;
    }

    public void setActionIntervalCount(int actionIntervalCount) {
        this.actionIntervalCount = actionIntervalCount;
    }

    public synchronized void addActionIntervalCount() {
        this.actionIntervalCount = this.actionIntervalCount + 1;
    }

    public synchronized boolean ifTakeAction() {
        addActionIntervalCount();
        if (this.actionIntervalCount < this.actionInterval) {
            return false;
        } else if (this.actionIntervalCount == this.actionInterval) {
            this.actionIntervalCount = 0;
            return true;
        }
        System.out.println("error in if take action ().");
        return false;
    }

    private volatile int actionIntervalCount = 0;

    public synchronized void setCurrentUtility(double currentUtility) {
        this.currentUtility = currentUtility;
    }

    public synchronized void setNextUtility(double nextUtility) {
        this.nextUtility = nextUtility;
    }

    private volatile double currentUtility;
    private volatile double nextUtility;


    private QLearningMetric() {

    }

    public synchronized void recordUtilityIncrease() {
        this.utilityIncrease = this.nextUtility - this.currentUtility;
    }

    public double getUtilityIncrease() {
        return utilityIncrease;
    }


    public synchronized void setQValue(int state, int action, double q) {
        Qtable.get(state)[action] = q;
    }

    public double getQValue(int state, int action) {
        return this.Qtable.get(state)[action];
    }


    public synchronized void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public synchronized void setQ(int s, int a, double value) {
//        if(Qtable.size() <= s) Qtable.add(new double[actionsCount]);
        Qtable.get(s)[a] = value;
    }

    public synchronized double getmaxQ(double currentCpuUsage,double currentLoad, double totalQps, double Rt, int curThreadNum) {

        int stateC = new Double(currentCpuUsage / 0.1).intValue();
        int stateL = new Double(currentLoad / 0.1).intValue();
        int stateQ = new Double(totalQps / 100).intValue();
        int stateR = new Double(Rt / 1).intValue();
        int stateT = new Double(curThreadNum / 50).intValue();

        String nextState = stateC + "#" + stateL + "#" + stateQ + "#" + stateR + "#" + stateT;
        if (!statesMap.containsKey(nextState)){
            return 0;
        }
        else {
            int nextStateIndex = statesMap.get(nextState);

            double maxValue = Double.MIN_VALUE;
            for (int i = 0; i < actionsCount; i++) {
                double value = this.Qtable.get(nextStateIndex)[i];

                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        }
    }

    public int getTrainNum() {
        return trainNum;
    }

    public synchronized void addTrainNum() {
        this.trainNum++;
    }

    public int getMaxTrainNum() {
        return maxTrainNum;
    }

    public synchronized boolean isTrain() {
        if (this.trainNum <= this.maxTrainNum) {
            this.isTrain = true;
        } else {
            this.isTrain = false;
        }
        return isTrain;
    }

    public synchronized void setTrain(boolean train) {
        this.isTrain = train;
    }

    public int getUpdateIntervalCount() {
        return updateIntervalCount;
    }

    public synchronized void setUpdateIntervalCount(int updateIntervalCount) {
        this.updateIntervalCount = updateIntervalCount;
    }

    public synchronized void addUpdateIntervalCount() {
//        System.out.print(updateIntervalCount + " ------------------- ");
        this.updateIntervalCount = updateIntervalCount + 1;
//        System.out.println(updateIntervalCount);
    }

    public void showPolicy() {
        int from;
        int to;
        statesCount = statesMap.size();
        System.out.println("\n ======= Show Policy =======" + statesCount);

        for (int i = 0; i < statesCount; i++) {
            from = i;
            to = policy(from);
            System.out.println("Current State: " + from + "       Action: " + actionNames[to] + "        Q: " + this.Qtable.get(from)[to]);
        }
    }

    // get policy from state
    public int policy(int state) {
        double maxValue = Double.MIN_VALUE;
        // default goto self if not found
        int policyGotoAction = 0;

        for (int i = 0; i < actionsCount; i++) {
            int action = i;
            double value = Qtable.get(state)[action];  //改成选取arraylist里面的值。

            if (value > maxValue) {
                maxValue = value;
                policyGotoAction = action;
            }
        }
        setAction(policyGotoAction);
        return policyGotoAction;
    }

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

    public double getBeta() {
        return beta;
    }

    public double getDelta() {
        return delta;
    }

    public double getGamma() {
        return gamma;
    }

    public boolean isQLearning() {
        return isQLearning;
    }

    public synchronized void setQLearning(boolean isQLearning) {
        this.isQLearning = isQLearning;
    }

    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }

    public synchronized void updateQ(double avgRt, double totalQps, int curThreadNum) {

        double q = getQValue(stateIndex, action);
        //执行action之后的下一个state属于哪个state。
        //locateNextState();

        double cpuUsage = SystemRuleManager.getCurrentCpuUsage();
        double load = SystemRuleManager.getCurrentSystemAvgLoad();
//        int nextState = locateState();

        double maxQ = getmaxQ(cpuUsage,load,avgRt, totalQps, curThreadNum);

//        System.out.println(" getreward() " + getReward());

        double qValue = q + delta * (getReward() + gamma * maxQ - q);//delta决定了奖励和下一状态的期望值的影响程度 gamma决定了maxQ的影响程度

//        System.out.println(" stateIndex = " + stateIndex + " original q value = " + q + " update q value = " + qValue );

        setQ(stateIndex, action, qValue);
    }

    /**
     * 通过Cpu使用率来判断当前所处的状态，返回值为qlearning算法里的当前状态
     * Judging current state by cpu usage，it is current state which is in the Qlearning algorithm。
     *
     * @return
     */
    public synchronized int locateState(double currentCpuUsage,double currentLoad, double totalQps, double Rt, int curThreadNum) {

        int stateC = new Double(currentCpuUsage / 0.1).intValue();
        int stateL = new Double(currentLoad / 0.1).intValue();
        int stateQ = new Double(totalQps / 100).intValue();
        int stateR = new Double(Rt / 1).intValue();
        int stateT = new Double(curThreadNum / 50).intValue();

        int stateIndex;

        String currentState = stateC + "#" + stateL + "#" + stateQ + "#" + stateR + "#" + stateT;
        if (!statesMap.containsKey(currentState)){
//                String.valueOf(stateC) + "#" + String.valueOf(stateL) + "#" + String.valueOf(stateQ) + "#" + String.valueOf(stateR) + "#" + String.valueOf(stateT))){
//            setStateC(stateC);
//            setStateL(stateL);
//            setStateQ(stateQ);
//            setStateR(stateR);
//            setStateT(stateT);
            stateIndex = statesMap.size();
            statesMap.put(currentState, stateIndex);
            Qtable.add(new double[actionsCount]);
        }
        else{
            stateIndex = statesMap.get(currentState);
        }
        setStateIndex(stateIndex);
        return stateIndex;

//        double currentload = 1;//后面删掉它 重新构建currentload
//        double interval = 1 / getStateSum();
//        double stateNum = currentCpuUsage / interval;
//        int i = new Double(stateNum).intValue();//double 转int的问题
//        int j = new Double(currentload / interval).intValue();
//
//        if (i < 0 | j < 0) {
//            setState(0);
//        } else {
//            setState(i * 10 + j + 1);
//        }
//        return getState();

//        if (0 <= currentCpuUsage && currentCpuUsage < 0.25) {
//            setState(1);
//            return getState();
//        }
//        if (0.25 <= currentCpuUsage && currentCpuUsage < 0.5) {
//            setState(2);
//            return getState();
//        }
//        if (0.5 <= currentCpuUsage && currentCpuUsage < 0.75) {
//            setState(3);
//            return getState();
//        }
//        if (0.75 <= currentCpuUsage && currentCpuUsage <= 1) {
//            setState(4);
//            return getState();
//        }
//
//        setState(0);
//        return getState();
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


}
