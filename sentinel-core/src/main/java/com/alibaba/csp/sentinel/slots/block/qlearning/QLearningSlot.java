package com.alibaba.csp.sentinel.slots.block.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.qlearning.QLearningMetric;
import com.alibaba.csp.sentinel.qlearning.QLearningUpdateManager;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

public class QLearningSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    static QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized, Object... args)
            throws Throwable {
//        System.out.println("Current context: " + context.getName());
//        System.out.println("Current entry resource: " + context.getCurEntry().getResourceWrapper().getName());
        try {
            QLearningUpdateManager qLearningUpdateManager = new QLearningUpdateManager();//管理存储 和 执行 qlearning的方法
            //检查已经执行了行为后是否得到效用，需要更新Q值
            qLearningUpdateManager.qLearningUpdate(Constants.ENTRY_NODE.successQps(),Constants.ENTRY_NODE.avgRt(),Constants.ENTRY_NODE.totalQps(),Constants.ENTRY_NODE.curThreadNum());
//        System.out.println(" in take action ().");
//        System.out.println("Current context: " + context.getName());

            qLearningUpdateManager.takeAction(resourceWrapper.getName(),Constants.ENTRY_NODE.totalQps(),Constants.ENTRY_NODE.avgRt(),Constants.ENTRY_NODE.curThreadNum());

//        System.out.println("Exiting for entry on QLearningSlot: " + context.getCurEntry().getResourceWrapper().getName());
            // 注意：要改，因为action初始化为0，必然block前10个请求。
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(qLearningMetric.isQLearning()){
            if(qLearningMetric.getAction() == 0){
//            System.out.println("- block - " + qLearningMetric.getActionIntervalCount());
                //改成AdaptiveException
                throw new SystemBlockException(resourceWrapper.getName(), "q-learning");
            }
        }


        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {

        fireExit(context, resourceWrapper, count, args);
    }

}
