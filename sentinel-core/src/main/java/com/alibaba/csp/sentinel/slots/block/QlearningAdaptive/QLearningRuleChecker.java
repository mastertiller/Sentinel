package com.alibaba.csp.sentinel.slots.block.QlearningAdaptive;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.util.Set;

public class QLearningRuleChecker {
    public void checkQlearning (ResourceWrapper resource, Context context, DefaultNode node, int count)//主要具体用途 如何检查规则
            throws BlockException {
        Set<QLearningRule> rules = QLearningRuleManager.getRules(resource);//对所有检查的流程搞清楚
        if (rules == null || resource == null) {
            return;
        }

        for (QLearningRule rule : rules) {
            if (!canPassCheck(rule, context, node, count)) { // 看懂目的 为啥要这么做 边看边想 涉及到学姐的就要研究一下自定义方法
                throw new QException(rule.getLimitApp(), rule);
            }
        }
    }

    public boolean canPassCheck(QLearningRule rule, Context context, DefaultNode node, int acquireCount) {

        Node selectedNode = node.getClusterNode();
        if (selectedNode == null) { return true; }

        return rule.getRater().canPass(selectedNode, acquireCount, false);
    }
}
