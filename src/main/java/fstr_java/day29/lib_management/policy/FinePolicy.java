package fstr_java.day29.lib_management.policy;

import fstr_java.day29.lib_management.common.FinePolicyType;

import java.math.BigDecimal;

public interface FinePolicy {

    FinePolicyType getType();

    BigDecimal calculateFine(long overdueDays);
}