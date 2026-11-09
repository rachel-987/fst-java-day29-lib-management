package fstr_java.day29.lib_management.policy.impl;

import fstr_java.day29.lib_management.common.FinePolicyType;
import fstr_java.day29.lib_management.policy.FinePolicy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DailyFinePolicy implements FinePolicy {

    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(5000);

    @Override
    public FinePolicyType getType() {
        return FinePolicyType.DAILY;
    }

    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        return DAILY_RATE.multiply(BigDecimal.valueOf(overdueDays));
    }
}