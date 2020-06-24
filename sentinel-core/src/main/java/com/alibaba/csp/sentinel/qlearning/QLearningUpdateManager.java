package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 该类包含了很多更新Q值的方法
 */
public class QLearningUpdateManager {
    QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    public int currentState;

    public double currentUtility;
    public double nextUtility;
    public double utilityIncrease;

    int randActionValue;



    public void takeAction(String resourceWrapperName){

        //Using Qlearning or BBR?
        if (qLearningMetric.isQLearning() && qLearningMetric.ifTakeAction()) {
            qLearningMetric.addTrainNum();

            int actionValue = chooseAction();

            //通过改变QPS限流规则来更改Accept和Block的数量
            List<FlowRule> oldRules = qLearningMetric.getRules();
            List<FlowRule> newRules = new ArrayList<>();
            for (FlowRule r : oldRules) {
                newRules.add(r);
            }
            FlowRule rule1 = new FlowRule();
            rule1.setResource(resourceWrapperName);
            rule1.setCount(Constants.ENTRY_NODE.successQps() + actionValue);
            rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rule1.setLimitApp("default");
            newRules.add(rule1);
            FlowRuleManager.loadRules(newRules);
//            if (!chooseAction(currentState)) {
////                //throw new SystemBlockException(resourceWrapper.getName(), "q-learning");
//
//            }
        }
    }



    /**
     *如果Qlearning正在训练，则随机选择动作，如果action = 0 执行block 如果 action= 1.执行accept
     *
     */
    public int chooseAction() {
        if (qLearningMetric.isTrain()) {
            //如果Qlearning正在训练，则随机选择动作，如果action = 0 执行block 如果 action= 1.执行accept

//        System.out.println("**************************" + randAction);
            randActionValue = qLearningMetric.randomActionValue();
            return randActionValue;
        } else {
            //会从已经训练出来的policy当中选出 最大奖励期望值的action
            currentState = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage());
            int bestActionValue = qLearningMetric.policy(currentState);
            return bestActionValue;

        }
    }



    /**
     * 执行完Action并发挥效用后，计算奖励并更新Q值
     */
    public void qLearningUpdate(double successQPS, double avgRt) {
        if (qLearningMetric.isQLearning() && qLearningMetric.isTrain() && qLearningMetric.isUpdate()) {
            if (qLearningMetric.getTrainNum() <= qLearningMetric.getMaxTrainNum()) {

                // 记录当前的增量。
                recordUtilityIncrease(successQPS, avgRt);

                qLearningMetric.updateQ();
            } else {
                qLearningMetric.setTrain(false);
                //System.out.println("-------------------TRAINING END--------------------");
                //qLearningMetric.showPolicy();
                //System.out.println(" ");
            }
        }
    }

    private void recordUtilityIncrease(double successQPS, double avgRt) {
        nextUtility = qLearningMetric.calculateUtility(successQPS, avgRt);
        utilityIncrease = nextUtility - currentUtility;
        qLearningMetric.setUtilityIncrease(utilityIncrease);
    }

    public void setCurrentUtility(double successQPS,double avgRt){
        currentUtility = qLearningMetric.calculateUtility(successQPS,avgRt);
    }


}
