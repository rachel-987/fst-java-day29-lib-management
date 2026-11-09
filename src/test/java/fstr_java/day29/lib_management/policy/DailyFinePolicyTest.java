package fstr_java.day29.lib_management.policy;

import fstr_java.day29.lib_management.policy.impl.DailyFinePolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailyFinePolicyTest {

    private final FinePolicy policy = new DailyFinePolicy();

    @Test
    void shouldCalculateFineForThreeOverdueDays() {
        BigDecimal result = policy.calculateFine(3);

        assertEquals(
                BigDecimal.valueOf(15000),
                result
        );
    }

    @Test
    void shouldReturnZeroWhenNotOverdue() {
        BigDecimal result = policy.calculateFine(0);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldReturnZeroForNegativeDays() {
        BigDecimal result = policy.calculateFine(-2);

        assertEquals(BigDecimal.ZERO, result);
    }
}