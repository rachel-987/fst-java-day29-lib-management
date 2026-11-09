package fstr_java.day29.lib_management.policy;

import fstr_java.day29.lib_management.policy.impl.FixedFinePolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedFinePolicyTest {

    private final FinePolicy policy = new FixedFinePolicy();

    @Test
    void shouldChargeFixedAmountWhenOverdue() {
        assertEquals(
                BigDecimal.valueOf(20000),
                policy.calculateFine(3)
        );
    }

    @Test
    void shouldNotChargeWhenReturnedOnTime() {
        assertEquals(
                BigDecimal.ZERO,
                policy.calculateFine(0)
        );
    }
}