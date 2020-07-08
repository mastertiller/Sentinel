package com.alibaba.csp.sentinel.slots.block.QlearningAdaptive;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public class QException extends BlockException {

    public QException (String ruleLimitApp) { super(ruleLimitApp); }

    public QException (String ruleLimitApp, QLearningRule rule) { super(ruleLimitApp, rule); }

    public QException (String message, Throwable cause) { super(message, cause); }

    public QException (String ruleLimitApp, String message) { super(ruleLimitApp, message); }

    @Override
    public Throwable fillInStackTrace() { return this; }

    /**
     * Get triggered rule.
     * Note: the rule result is a reference to rule map and SHOULD NOT be modified.
     *
     * @return triggered rule
     * @since 1.4.2
     */
    @Override
    public QLearningRule getRule() { return rule.as(QLearningRule.class); }
}
