package com.alibaba.csp.sentinel.slots.block.QlearningAdaptive;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class QLearningRuleManager {

    private static final Map<String, Set<QLearningRule>> QlearningRules = new ConcurrentHashMap<String, Set<QLearningRule>>();

    private static final RulePropertyListener LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<QLearningRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }


    public static Set<QLearningRule> getRules(ResourceWrapper resource) {
        return QlearningRules.get(resource.getName());
    }

    public static void loadRules(List<QLearningRule> rules) {
        try{
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.warn("[QLearningRuleManager] Unexpected error when loading Qlearning rules", e);
        }
    }

    private static class RulePropertyListener implements PropertyListener<List<QLearningRule>> {

        @Override
        public void configUpdate(List<QLearningRule> conf) {
            Map<String, Set<QLearningRule>> rules = QLearningRuleUtil.buildAdaptiveRuleMap(conf);
            if (rules != null) {
                QlearningRules.clear();
                QlearningRules.putAll(rules);
            }
            RecordLog.info("[QLearningRuleManager] Qlearning rules received: " + QlearningRules);
        }

        @Override
        public void configLoad(List<QLearningRule> conf) {
            Map<String, Set<QLearningRule>> rules = QLearningRuleUtil.buildAdaptiveRuleMap(conf);
            if (rules != null) {
                QlearningRules.clear();
                QlearningRules.putAll(rules);
            }
            RecordLog.info("[QLearningRuleManager] Qlearning rules received: " + QlearningRules);
        }
    }

}
