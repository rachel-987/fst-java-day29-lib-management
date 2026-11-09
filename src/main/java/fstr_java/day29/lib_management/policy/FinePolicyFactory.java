package fstr_java.day29.lib_management.policy;

import fstr_java.day29.lib_management.common.FinePolicyType;
import fstr_java.day29.lib_management.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FinePolicyFactory {

    private final Map<FinePolicyType, FinePolicy> policies;

    public FinePolicyFactory(List<FinePolicy> finePolicies) {

        Map<FinePolicyType, FinePolicy> policyMap = new EnumMap<>(FinePolicyType.class);

        for (FinePolicy policy : finePolicies) {
            FinePolicy previous = policyMap.putIfAbsent(policy.getType(), policy);

            if (previous != null) {
                throw new IllegalStateException("Duplicate fine policy: " + policy.getType());
            }
        }

        this.policies = Map.copyOf(policyMap);
    }

    public FinePolicy getPolicy(FinePolicyType type) {

        FinePolicy policy = policies.get(type);

        if (policy == null) {
            throw new BusinessException("Unsupported fine policy: " + type, HttpStatus.BAD_REQUEST);
        }

        return policy;
    }
}