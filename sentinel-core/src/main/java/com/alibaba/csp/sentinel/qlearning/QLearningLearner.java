package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.util.TimeUtil;

public class QLearningLearner {
    QLearningMetric qLearningMetric = new QLearningMetric().getInstance();
    QInfo qInfo;

    public void learn() {
        if (checkUpdate()) {
            //执行action
            System.out.println("start tick —— " + qLearningMetric.getCt()
                    + "    start count —— " + qLearningMetric.getCn());
            if(containsQInfo()){
                //取，
                //更新
                UpdateQ();
                System.out.println("取走： " + qLearningMetric.getBi() + "    " + qLearningMetric.getAction());
            }
            //开始下一批决策
            qLearningMetric.addBi();
            //决策
            qInfo = takeAction(node);
            //存
            qLearningMetric.putHm(qLearningMetric.getBi(),qInfo);
            System.out.println("存下： " + qLearningMetric.getBi() + "   " + qLearningMetric.getAction());
        }
    }

    private synchronized boolean checkUpdate() {
//        System.out.println(qLearningMetric.getCt());
        long ct = qLearningMetric.getCt();
        long t = TimeUtil.currentTimeMillis();

        qLearningMetric.addCn();

        if(ct <= 0){
            qLearningMetric.setCt(TimeUtil.currentTimeMillis());
//            System.out.println("start tick —— " + qLearningMetric.getCt()
//            + "    start count —— " + qLearningMetric.getCn());
            return true;
        }
        if(t-ct >= 20 || qLearningMetric.getCn() >= 200){

            System.out.println("bombing! —— " + (t-ct)
                    + "    total count —— " + qLearningMetric.getCn());
//            System.out.println(" ____________________________________________");
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
//            System.out.println("存进去了" + bi);
            return true;
        }
        else{
            //测试
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        }
        return false;
    }

    private void UpdateQ() {

        qInfo = qLearningMetric.pushQInfo();
        String s = qInfo.getState();
        int a = qInfo.getAction();
        double u = qInfo.getUtility();
        double nextUtility = qLearningMetric.calculateUtility(cpu,rt,...);
        int r = qLearningMetric.getReward(u,nextUtility);
        double q = qLearningMetric.getQValue(s,a);
        double maxQ = qLearningMetric.getMaxQ();
        double qUpdated = qLearningMetric.qUpdate(q,r,maxQ);
        qLearningMetric.setQValue(s,a,qUpdated);
    }

    private synchronized QInfo takeAction(DefaultNode node) {

        String s = qLearningMetric.locateState(node);
        int a;
        if(qLearningMetric.isTrain()){
            //随机选择
            a = qLearningMetric.getRandomAction();
        }
        else{
            //从qtable中选择
            a = qLearningMetric.policy();
        }
        qLearningMetric.setAction(a);
        double u = qLearningMetric.calculateUtility(node);

//        测试用
//        String s = "x";
//        int a = (qLearningMetric.getBi()) % 2;
//        double u = 2.2;
//
//        if(a > 1 || a < 0){
//            System.out.println("--------------------- error in a ");
//        }
        qLearningMetric.setAction(a);
        QInfo qInfo = new QInfo();
        qInfo.setQInfo(s,a,u);

        return qInfo;
    }


}
