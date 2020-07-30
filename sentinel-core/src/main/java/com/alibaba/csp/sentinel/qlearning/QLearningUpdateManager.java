package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 该类包含了很多更新Q值的方法
 */
public class QLearningUpdateManager {
    QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    public String currentState;//这里的currentstate为int类型，不同于metric里的

    public double currentUtility;
    public double nextUtility;
    public double utilityIncrease;


    public synchronized void takeAction(String resourceWrapperName, double totalQps, double avgRt, int curThreadNum){

        // 不用判断isTrain().仅当QLearning为true，且actionIntervalCount为10的倍数时，返回true。

        if (qLearningMetric.isQLearning() && qLearningMetric.ifTakeAction()) {

            qLearningMetric.addTrainNum();

            TemptQInfo temptQInfo = new TemptQInfo();
            int action = chooseAction(totalQps,avgRt,curThreadNum,temptQInfo);
//            if (!qLearningMetric.isTrain()) {
//                System.out.println(actionValue);
//            }

            double currentUtility = setCurrentUtility(Constants.ENTRY_NODE.successQps(), Constants.ENTRY_NODE.avgRt(),temptQInfo);
//            temptQInfo.setUtility(currentUtility);
//            qLearningMetric.addQInfo(temptQInfo);

//            //通过改变QPS限流规则来更改Accept和Block的数量
//            List<FlowRule> oldRules = qLearningMetric.getRules();
//            List<FlowRule> newRules = new ArrayList<>();
//            for (FlowRule r : oldRules) {
//                newRules.add(r);
//            }
//            FlowRule rule1 = new FlowRule();
//            rule1.setResource(resourceWrapperName);
//            rule1.setCount(Constants.ENTRY_NODE.maxSuccessQps() + actionValue);
//            rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
//            rule1.setLimitApp("default"); //注意要与oldRules一致。
//            newRules.add(rule1);
//            FlowRuleManager.loadRules(newRules);
//            List<SystemRule> rules = new ArrayList<SystemRule>();
//            SystemRule rule = new SystemRule();
//            rule.setQps(Constants.ENTRY_NODE.maxSuccessQps() + actionValue);
//            rules.add(rule);
//        SystemRuleManager.loadRules(Collections.singletonList(rule));
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
    public synchronized int chooseAction(double totalQps, double avgRt, int curThreadNum,TemptQInfo temptQInfo) {
        //转换成int来操作,
        currentState = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage(),SystemRuleManager.getCurrentSystemAvgLoad(),Constants.ENTRY_NODE.totalQps(),Constants.ENTRY_NODE.avgRt(),Constants.ENTRY_NODE.curThreadNum());
        temptQInfo.setState(currentState);
        //        System.out.println(" chooseAction .");
        if (qLearningMetric.isTrain()) {
            //如果Qlearning正在训练，则随机选择动作，如果action = 0 执行block 如果 action= 1.执行accept
            int randAction = qLearningMetric.randomActionValue();
//            System.out.println(" ** " + randAction + " ** " + qLearningMetric.getActionIntervalCount() + " ** ");
            temptQInfo.setAction(randAction);
            return randAction;
        } else {
            //会从已经训练出来的policy当中选出 最大奖励期望值的action
            int bestAction = qLearningMetric.policy(currentState);
//            System.out.println("   bestAction: " + bestActionValue);
            temptQInfo.setAction(bestAction);
            return bestAction;
        }
    }



    /**
     * 执行完Action并发挥效用后，计算奖励并更新Q值
     */
    public synchronized void qLearningUpdate(double successQPS, double avgRt, double totalQps, int curThreadNum) {
//    public void qLearningUpdate(double successQPS, double avgRt, double totalQps, int curThreadNum) {
//        System.out.println("Current context。");
        if (qLearningMetric.isQLearning() && qLearningMetric.isUpdate()) {
            // 记录当前的增量。
//            System.out.println("Current context: ");
            recordUtilityIncrease(successQPS, avgRt);
//            System.out.println("Current context: ");
            qLearningMetric.updateQ(avgRt, totalQps, curThreadNum);
//            System.out.println("Current context: ");
//            } else {
//                qLearningMetric.setTrain(false);
//                //System.out.println("-------------------TRAINING END--------------------");
//                //qLearningMetric.showPolicy();
//                //System.out.println(" ");
//            }
        }
    }

    private synchronized void recordUtilityIncrease(double successQPS, double avgRt) {
        nextUtility = qLearningMetric.calculateUtility(successQPS, avgRt);
        qLearningMetric.setNextUtility(nextUtility);
//        System.out.println("nextUtility = " + nextUtility);
        qLearningMetric.recordUtilityIncrease();
    }

    public synchronized double setCurrentUtility(double successQPS,double avgRt,TemptQInfo temptQInfo){
        currentUtility = qLearningMetric.calculateUtility(successQPS,avgRt);
        temptQInfo.setUtility(currentUtility);
        qLearningMetric.addQInfo(temptQInfo);

        qLearningMetric.setCurrentUtility(currentUtility);
//        System.out.println("currentUtility = " + currentUtility);
        return currentUtility;
    }


}
