package com.alibaba.csp.sentinel.qlearning;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class QLearningMetric {
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

    public void addBi() {
        bi.incrementAndGet();
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
        this.hm.put(i,qInfo);
    }

    public QInfo pushQInfo() {
        return this.hm.get(this.bi);
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

        String currentState = stateC + "#" + stateQ + "#" + stateR + "#" + stateT;

        if (!Qtable.containsKey(currentState)) {
            if (!isTrain()) {
                newStateCount++;
//                System.out.print("new State: " + currentState);
            }
//            System.out.println("training - new State: " + currentState);
            Qtable.put(currentState, new double[actionsCount]);
            //返回current state
        } else if (Qtable.containsKey(currentState)) {//这里需要考虑再加一些其他因果 但是我暂时没想好 等结合了那个新的code再去改好了
            if (!isTrain()) {
                oldStateCount++;
//                System.out.print("old State: " + currentState);
            }
//            System.out.println("training - old State: " + currentState);
//             stateIndex = Qtable.get(currentState);
        } else {
            System.out.println("Error in get state from Q table.");
        }
//        this.state = currentState;
        return currentState;
    }



    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }



}
