package com.alibaba.csp.sentinel.qlearning;

public class TemptQInfo {
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public double getUtility() {
        return utility;
    }

    public void setUtility(double utility) {
        this.utility = utility;
    }

    private String state;
    private int action;
    private double utility;

}
