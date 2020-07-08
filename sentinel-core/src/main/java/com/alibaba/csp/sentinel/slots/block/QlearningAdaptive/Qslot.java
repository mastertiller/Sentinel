package com.alibaba.csp.sentinel.slots.block.QlearningAdaptive;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;

public class Qslot extends AbstractLinkedProcessorSlot<DefaultNode> {
    private final QLearningRuleChecker checker;

    public Qslot () {//初始化slot
        this(new QLearningRuleChecker());
    }

    Qslot (QLearningRuleChecker checker) {//返回错误信息
        AssertUtil.notNull(checker, "QLearningRuleChecker should not be null");
        this.checker = checker;
    }

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized, Object... args)//做什么用
            throws Throwable {
        checkQLearning(resourceWrapper, context, node, count);
        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    void checkQLearning (ResourceWrapper resource, Context context, DefaultNode node, int count)
            throws BlockException {
        checker.checkQlearning(resource, context, node, count);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {//离开
        fireExit(context, resourceWrapper, count, args);
    }

}
