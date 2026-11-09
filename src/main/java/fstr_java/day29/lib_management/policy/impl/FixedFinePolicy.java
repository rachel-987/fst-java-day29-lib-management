package fstr_java.day29.lib_management.policy.impl;

import fstr_java.day29.lib_management.common.FinePolicyType;
import fstr_java.day29.lib_management.policy.FinePolicy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FixedFinePolicy implements FinePolicy {

    private static final BigDecimal FIXED_RATE = BigDecimal.valueOf(20000);

    @Override
    public FinePolicyType getType() {
        return FinePolicyType.FIXED;
    }

    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        return FIXED_RATE;
    }
}