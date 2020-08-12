package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

public class QLearningLearner {
    QLearningMetric qLearningMetric = new QLearningMetric().getInstance();
    private int bacthNum = 200;
    private long batchTime = 20;

    public synchronized void learn(ResourceWrapper resourceWrapper,DefaultNode node) throws SystemBlockException {
        try {
            if (checkUpdate()) {
                if (containsQInfo() && qLearningMetric.isTrain()) {
                    UpdateQ(node);
                }
                //开始下一批决策
                int bi = qLearningMetric.addBi();
                //决策
                QInfo qInfo = takeAction(node);

                qLearningMetric.putHm(bi, qInfo);

            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean checkUpdate() {

        long ct = qLearningMetric.getCt();
        long t = TimeUtil.currentTimeMillis();

        qLearningMetric.addCn();

        if(ct <= 0){
            qLearningMetric.setCt(TimeUtil.currentTimeMillis());

            return true;
        }
        if(t-ct >= batchTime || qLearningMetric.getCn() >= bacthNum){

            qLearningMetric.setCt(TimeUtil.currentTimeMillis());
            qLearningMetric.resetCn();
            return true;
        }
        return false;
    }

    private boolean containsQInfo() {
        int bi = qLearningMetric.getBi();

        if(bi == 0) {
            return false;
        }

        if(qLearningMetric.getHm().containsKey(bi)){
            return true;
        }

        return false;
    }

    private synchronized void UpdateQ(DefaultNode node) {

        QInfo qInfo = qLearningMetric.pushQInfo();
        String s = qInfo.getState();
        int a = qInfo.getAction();
        double u = qInfo.getUtility();

        double nextUtility = qLearningMetric.calculateUtility(node.successQps(),node.avgRt());

        int r = qLearningMetric.getReward(u,nextUtility);
        double q = qLearningMetric.getQValue(s,a);
        double maxQ = qLearningMetric.getMaxQ(SystemRuleManager.getCurrentCpuUsage(),node.passQps(),node.avgRt(),0);
        double qUpdated = qLearningMetric.updateQ(q,r,maxQ);
        qLearningMetric.setQValue(s,a,qUpdated);
    }

    private synchronized QInfo takeAction(DefaultNode node) {

        String s = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage(),node.passQps(),node.avgRt(),0);
        int a;
        if(qLearningMetric.isTrain()){
            //随机选择
            a = qLearningMetric.getRandomAction();

        }
        else{
            //从qtable中选择
            a = qLearningMetric.policy(s);
        }

        qLearningMetric.setAction(a);

        double u = qLearningMetric.calculateUtility(node.successQps(),node.avgRt());

        QInfo qInfo = new QInfo();
        qInfo.setQInfo(s,a,u);

        return qInfo;
    }


}
