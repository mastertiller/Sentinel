package com.alibaba.csp.sentinel.slots.block.qlearning;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;

public class QSlotChainBuilderSpiDemo {
    public static void main(String[] args) {
        // You will see this in record.log, indicating that the custom slot chain builder is activated:
        // [SlotChainProvider] Global slot chain builder resolved: com.alibaba.csp.sentinel.demo.slot.DemoSlotChainBuilder
        Entry entry = null;
        try {
            entry = SphU.entry("abc");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
