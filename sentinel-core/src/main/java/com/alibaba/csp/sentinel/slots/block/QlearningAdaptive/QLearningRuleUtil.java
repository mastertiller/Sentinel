package com.alibaba.csp.sentinel.slots.block.QlearningAdaptive;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class QLearningRuleUtil {
    public static Map<String, Set<QLearningRule>> buildAdaptiveRuleMap(List<QLearningRule> list) {

        Map<String, Set<QLearningRule>> newRuleMap = new ConcurrentHashMap<>();

        if (list == null || list.isEmpty()) {
            return newRuleMap;
        }

        for (QLearningRule rule : list) {
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }

            TrafficShapingController rater = generateRater(rule);
            rule.setRater(rater);

            String identity = rule.getResource();
            Set<QLearningRule> ruleSet = newRuleMap.get(identity);
            if (ruleSet == null) {
                ruleSet = new HashSet<>();
                newRuleMap.put(identity, ruleSet);
            }
            ruleSet.add(rule);
        }
        return newRuleMap;
    }

    /**
     这个是controller里所需要搭建的
     */
    private static TrafficShapingController generateRater(QLearningRule rule) {

//         根据expectRt判断是适合用漏桶还是用令牌桶
//        if (rule.getExpectRt() <= 200) {
//            return new TokenBucketController(rule.getTargetRatio(), rule.getExpectRt());
//            //return new PidController(rule.getTargetRatio(), rule.getExpectRt());
//        } 修改部分
         return null;
//        new QLearningRateController((int)rule.getExpectRt());
    }

}
