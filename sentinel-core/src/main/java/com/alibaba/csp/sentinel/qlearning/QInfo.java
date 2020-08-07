package com.alibaba.csp.sentinel.qlearning;

import java.util.concurrent.atomic.AtomicInteger;

public class QInfo {
    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getAction() {
        return action.get();
    }

    public void setAction(int action) {
        this.action.set(action);
    }

    public double getUtility() {
        return utility;
    }

    public void setUtility(double utility) {
        this.utility = utility;
    }

    String state;
    AtomicInteger action = new AtomicInteger(0);
    volatile double utility;

    public synchronized void setQInfo(String state, int action,double utility){
        this.state = state;
        this.action.set(action);
        this.utility = utility;
    }

}
