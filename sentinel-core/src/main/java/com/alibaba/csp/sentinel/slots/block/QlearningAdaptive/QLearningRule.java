package com.alibaba.csp.sentinel.slots.block.QlearningAdaptive;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;

public class QLearningRule extends AbstractRule {
    public void QLearingRule () {}

    public QLearningRule (String resourceName) {
        setResource(resourceName);
    }

    private double count;

    private int maxToken;

    private double targetRatio;

    private double expectRt;

    private TrafficShapingController controller;

    public double getCount () {
        return count;
    }

    public QLearningRule setCount (double count) {
        this.count = count;
        return this;
    }

    public int getMaxToken () {
        return maxToken;
    }

    public QLearningRule setMaxToken (int maxToken) {
        this.maxToken = maxToken;
        return this;
    }

    public double getTargetRatio () {
        return targetRatio;
    }

    public QLearningRule setTargetRatio (double targetRatio) {
        this.targetRatio = targetRatio;
        return this;
    }

    public double getExpectRt () {
        return expectRt;
    }

    public void setExpectRt (double expectRt) {
        this.expectRt = expectRt;
    }

    public TrafficShapingController getRater () {
        return controller;
    }

    QLearningRule setRater (TrafficShapingController rater) {
        this.controller = controller;
        return this;
    }


    @Override
   public boolean passCheck (Context context, DefaultNode node, int count, Object... args) { return true; }//自动生成的是false 但是学姐的是true
}
