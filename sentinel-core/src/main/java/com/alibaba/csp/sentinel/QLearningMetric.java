package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.qlearning.demo.QInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class QLearningMetric {
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

    /////////////////////////////////////////////////////////

    public ConcurrentHashMap<Integer,QInfo > getHm() {
        return hm;
    }

    public void putHm(int i,QInfo qInfo) {
        this.hm.put(i,qInfo);
    }

    private ConcurrentHashMap<Integer, QInfo> hm = new ConcurrentHashMap<>();

    //////////////////////////////////////////////////////

    public int getAction(){
        return this.action.get();
    }
    public void setAction(int a) {
        this.action.set(a);
    }

    private AtomicInteger action = new AtomicInteger(1);

    public QInfo pushQInfo() {
        return this.hm.get(this.bi);
    }


    private static class QLearningMetricContainer {
        private static QLearningMetric instance = new QLearningMetric();
    }

    public static QLearningMetric getInstance() {
        return QLearningMetricContainer.instance;
    }



}
