package com.alibaba.csp.sentinel.qlearning;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.qlearning.qtable.QTable;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.io.IOException;

public class QLearningLearner {
    QLearningMetric qLearningMetric = new QLearningMetric().getInstance();
    QTable qTable = new QTable();

    private static String qTablePath = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/qlearning/qtable/QTable-UserPeak-3.txt";

    private int bacthNum = 200;
    private long batchTime = 20;

    public QLearningLearner() throws IOException {
    }

    public synchronized void learn(ResourceWrapper resourceWrapper,DefaultNode node) throws SystemBlockException {
        try {
            if (checkUpdate()) {
                //每批第一个线程开始计时和计数
                if (containsQInfo() && qLearningMetric.isTrain()) {
                    //取，
                    //更新
                    UpdateQ(node);

                }
                //开始下一批决策
                int bi = qLearningMetric.addBi();
                qLearningMetric.addRtMap(bi,node.avgRt());
                //决策
                QInfo qInfo = takeAction(node);
                System.out.println("      状态: " + qInfo.getState() + "      决策: " + qInfo.getAction());
                //存
                qLearningMetric.putHm(bi, qInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (qLearningMetric.getAction() == 0) {
            throw new SystemBlockException(resourceWrapper.getName(), "q-learning");
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

            System.out.println("!bombing!     total time —— " + (t-ct)
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

        int r = qLearningMetric.getReward(a,u,nextUtility);
        double q = qLearningMetric.getQValue(s,a);
        double maxQ = qLearningMetric.getMaxQ(SystemRuleManager.getCurrentCpuUsage(),node.passQps(),node.avgRt(),0);
        double qUpdated = qLearningMetric.updateQ(q,r,maxQ);
        System.out.println(qLearningMetric.getBi() + "  batch END "  + "    ======== NEXT successQPS: " + node.successQps() + "     ======= NEXT RT: "  + node.avgRt() + "     ======= NEXT Utility : " + nextUtility);
        System.out.println(qLearningMetric.getBi() + "  batch END "  + " ======= STATE: " + s +" ======== Q: " + q + " ======== REWARD: " + r + " ======== maxQ: " + maxQ + " ======= Q UPDATE : " + qUpdated + " ----------------------------------------------------------------------------");
        System.out.println(" ");
        qLearningMetric.setQValue(s,a,qUpdated);
    }

    private synchronized QInfo takeAction(DefaultNode node) {

        String s = qLearningMetric.locateState(SystemRuleManager.getCurrentCpuUsage(),node.passQps(),node.avgRt(),0);
        int a;
        if(qLearningMetric.isTrain()){
            //随机选择
            a = qLearningMetric.getRandomAction();
            System.out.println("  随机决策 " );
        }
        else{
            //从qtable中选择
//            System.out.println("  ******************** Train Finish *********************");
//            qTable.save(qLearningMetric.getQtable(),qTablePath);
            a = qLearningMetric.policy(s);
        }

        qLearningMetric.setAction(a);

        double u = qLearningMetric.calculateUtility(node.successQps(),node.avgRt());

        System.out.println( qLearningMetric.getBi() + " batch BEGIN " + " --------- CPU usage: " + SystemRuleManager.getCurrentCpuUsage() +  "    ======== successQPS: " + node.successQps() + "    ======== RT: "  + node.avgRt() + "     ======= Utility : " + u);

//        System.out.println( qLearningMetric.getBi() + " batch BEGIN " + " --------- ThreadNum: " + Constants.ENTRY_NODE.curThreadNum() +  "    ======== successQPS: " + node.successQps() + "    ======== RT: "  + node.avgRt() + "     ======= Utility : " + u);

        QInfo qInfo = new QInfo();
        qInfo.setQInfo(s,a,u);

        return qInfo;
    }


}
