package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 该类为单例模式，保证了全局变量只能够访问一个公有对象 通过内部类来实现
 */
public class QLearningMetric {


    private boolean isQLearning;

    final int stateA = 0;
    final int stateB = 1;
    final int stateC = 2;
    final int stateD = 3;
    final int stateE = 4;

    final int acceptValue = 9;
    final int blockValue = acceptValue; //不相等的话无法判断何时更新Q值，需更改updateInterval和qLearningUpdateManager.isUpdate()


    final int[] states = new int[]{stateA, stateB, stateC, stateD, stateE};
    final int[] actionValues = new int[]{0,acceptValue, -1*blockValue};

    String[] stateNames = new String[]{"CPU Usage: UnGet", "CPU Usage: (0%, 25%)", "CPU Usage: (25%, 50%)", "CPU Usage: (50%, 75%)", "CPU Usage: (75%, 100%)"};
    String[] actionNames = new String[]{"Block", "Accept"};

    private volatile double utilityIncrease;
    private volatile int state;
    private volatile int action;

    private volatile int statesCount = states.length;


    public synchronized int randomActionValue(){
        int randValue = new Random().nextInt(actionsCount);
        setAction(randValue); //记录执行的动作
        return actionValues[randValue];
    }

    private volatile int actionsCount = actionValues.length;
    private volatile double[][] Q = new double[statesCount][actionsCount];

    private volatile int maxTrainNum = 200000;
    private volatile boolean isTrain = true;

    private volatile int trainNum = 0; //当前训练到第几次

    private double alpha = 1;//alpha控制了效用方程的qps的参数
    private double beta = 0.02;//控制了效用方程的RT的参数

    private double delta = 1;
    private double gamma = 1;

    private double tolerance = 0.01;



    private int rewardValue = 10;
    private int punishValue = -1;

    private int actionInterval = 10;

    public int getUpdateInterval() {
        return updateInterval;
    }

    private int updateInterval = acceptValue;
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

    public void addActionIntervalCount(){
        this.actionIntervalCount = this.actionIntervalCount + 1;
    }

    public synchronized boolean ifTakeAction(){
        addActionIntervalCount();
        if(this.actionIntervalCount < this.actionInterval){
            return false;
        }
        else if(this.actionIntervalCount == this.actionInterval){
            this.actionIntervalCount = 0;
            return true;
        }
        System.out.println("error in if take action ().");
        return false;
    }

    private int actionIntervalCount = 0;


    private QLearningMetric() {

    }

    public synchronized void setUtilityIncrease(double utilityIncrease) {
        this.utilityIncrease = utilityIncrease;
    }

    public double getUtilityIncrease() {
        return utilityIncrease;
    }

    public synchronized void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    public synchronized void setQValue(int state, int action, double q) {
        Q[state][action] = q;
    }

    public double getQValue(int state, int action) {
        return this.Q[state][action];
    }


    public synchronized void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setQ(int s, int a, double value) {
        Q[s][a] = value;
    }

    public double getmaxQ(int nextState) {

        double maxValue = Double.MIN_VALUE;
        for (int i = 0; i < actionsCount; i++) {
            double value = this.Q[nextState][i];

            if (value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
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

    public boolean isTrain() {
        return isTrain;
    }

    public synchronized void setTrain(boolean train) {
        this.isTrain = train;
    }

    public int getUpdateIntervalCount() {
        return updateIntervalCount;
    }

    public void setUpdateIntervalCount(int updateIntervalCount) {
        this.updateIntervalCount = updateIntervalCount;
    }

    public synchronized void addUpdateIntervalCount(){
        this.updateIntervalCount = this.updateIntervalCount + 1;
    }

    public void showPolicy() {
        System.out.println("\n ======= Show Policy =======");
        for (int i = 0; i < statesCount; i++) {
            int from = states[i];
            int to = policy(from);
            System.out.println("Current State: " + stateNames[from] + "       Action: " + actionNames[to] + "        Q: " + this.Q[from][to]);
        }
    }

    // get policy from state
    public int policy(int state) {
        double maxValue = Double.MIN_VALUE;
        // default goto self if not found
        int policyGotoAction = 0;

        for (int i = 0; i < actionsCount; i++) {
            int action = i;
            double value = Q[state][action];

            if (value > maxValue) {
                maxValue = value;
                policyGotoAction = action;
            }
        }
        return policyGotoAction;
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

    public synchronized void updateQ() {

        double q = getQValue(state, action);
        //执行action之后的下一个state属于哪个state。
        //locateNextState();

        double cpuUsage = SystemRuleManager.getCurrentCpuUsage();
        int nextState = locateState(cpuUsage);

        double maxQ = getmaxQ(nextState);

        double qValue = q + delta * (rewardValue + gamma * maxQ - q);//delta决定了奖励和下一状态的期望值的影响程度 gamma决定了maxQ的影响程度

        setQ(state, action, qValue);
    }

    /**
     *通过Cpu使用率来判断当前所处的状态，返回值为qlearning算法里的当前状态
     * Judging current state by cpu usage，it is current state which is in the Qlearning algorithm。
     *
     * @return
     */
    public synchronized int locateState(double currentCpuUsage) {

        if (0 <= currentCpuUsage && currentCpuUsage < 0.25) {
            setState(1);
            return getState();
        }
        if (0.25 <= currentCpuUsage && currentCpuUsage < 0.5) {
            setState(2);
            return getState();
        }
        if (0.5 <= currentCpuUsage && currentCpuUsage < 0.75) {
            setState(3);
            return getState();
        }
        if (0.75 <= currentCpuUsage && currentCpuUsage <= 1) {
            setState(4);
            return getState();
        }

        setState(0);
        return getState();
    }

    public synchronized int getReward() {
        if (getUtilityIncrease() > tolerance) {
            return rewardValue;
        } else if(getUtilityIncrease() < -1*tolerance){
            return punishValue;
        } else {
            return 0;
        }
    }

    public double calculateUtility(double successQPS, double avgRt){
        double utility = alpha * successQPS - beta * avgRt;
        return utility;
    }

    public boolean isUpdate(){
        if(getTrainNum() > 0) {
            addUpdateIntervalCount();
            if (getUpdateIntervalCount() < getUpdateInterval()) {
                return false;
            } else if (getUpdateIntervalCount() == getUpdateInterval()) {
                setUpdateIntervalCount(0);
                return true;
            }
            System.out.println(" error in isUpdate().");
        }
        return false;

    }


}
