package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

public class QLearningLearner {
    QLearningMetric qLearningMetric = new QLearningMetric().getInstance();
    private int bacthNum = 200;
    private long batchTime = 20;

//    QInfo qInfo = new QInfo();

    /**
    加入synchronized？？
     */
    public synchronized void learn(DefaultNode node) {
        if (checkUpdate()) {
            //每批第一个线程开始计时和计数
            System.out.println("start tick —— " + qLearningMetric.getCt()
                    + "    start count —— " + qLearningMetric.getCn());

            if(containsQInfo()){
                //取，
                //更新
                UpdateQ(node);
                System.out.println("取走： " + qLearningMetric.getBi() + "    " + qLearningMetric.getAction());
            }
            //开始下一批决策
            int bi = qLearningMetric.addBi();
            //决策
            QInfo qInfo = takeAction(node);
            //存
            qLearningMetric.putHm(bi,qInfo);

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
        if(t-ct >= batchTime || qLearningMetric.getCn() >= bacthNum){

            System.out.println("bombing! —— " + (t-ct)
                    + "    total count —— " + qLearningMetric.getCn());

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
            System.out.println("@@@@@ containsQInfo : 没存进去 @@@@@@ " + bi);
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
        double maxQ = qLearningMetric.getMaxQ(SystemRuleManager.getCurrentCpuUsage(),node.passQps(),node.avgRt(),node.curThreadNum());
        double qUpdated = qLearningMetric.updateQ(q,r,maxQ);
        System.out.println("  NEXT batch: " + qLearningMetric.getBi() + " --------- NEXT successQPS: " + node.successQps() + " ======= NEXT RT: "  + node.avgRt() + " ======= NEXT Utility : " + nextUtility);
         System.out.println(" ======= q :" + q + " ======== REWARD : " + r + " ======== maxQ : " + maxQ + " ======= q update : " + qUpdated );
        qLearningMetric.setQValue(s,a,qUpdated);
    }

    private synchronized QInfo takeAction(DefaultNode node) {

        String s = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage(),node.passQps(),node.avgRt(),node.curThreadNum());
        int a;
        if(qLearningMetric.isTrain()){
            //随机选择
            a = qLearningMetric.getRandomAction();

        }
        else{
            //从qtable中选择
            a = qLearningMetric.policy(s);
        }
        /**
         * TO DO: 尽量并入上面两种方法内
         */
        qLearningMetric.setAction(a);

        double u = qLearningMetric.calculateUtility(node.successQps(),node.avgRt());
        System.out.println(" last batch: " + qLearningMetric.getBi() + " --------- ThreadNum: " + Constants.ENTRY_NODE.curThreadNum() +  " ======== successQPS: " + node.successQps() + " ======= RT: "  + node.avgRt() + " ======= Utility : " + u);

//        测试用
//        String s = "x";
//        int a = (qLearningMetric.getBi()) % 2;
//        double u = 2.2;
//
//        if(a > 1 || a < 0){
//            System.out.println("--------------------- error in a ");
//        }
//        qLearningMetric.setAction(a);

        QInfo qInfo = new QInfo();
        qInfo.setQInfo(s,a,u);

        return qInfo;
    }


}
