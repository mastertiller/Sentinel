package com.alibaba.csp.sentinel.slots.block.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.qlearning.QInfo;
import com.alibaba.csp.sentinel.qlearning.QLearningLearner;
import com.alibaba.csp.sentinel.qlearning.QLearningMetric;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import sun.plugin.dom.html.HTMLFormElement;

public class QLearningSlot extends AbstractLinkedProcessorSlot<DefaultNode> {
    private QLearningLearner qLearningLearner = new QLearningLearner();

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized, Object... args)
            throws Throwable {
        qLearningLearner.learn(resourceWrapper,node);
        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {

        fireExit(context, resourceWrapper, count, args);
    }

}

