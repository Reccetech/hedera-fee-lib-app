package com.hedera.node.app.hapi.fees.apis.common;

import com.hedera.node.app.hapi.fees.AbstractFeeModel;
import com.hedera.node.app.hapi.fees.BaseFeeRegistry;
import com.hedera.node.app.hapi.fees.FeeResult;
import com.hedera.node.app.hapi.fees.ParameterDefinition;

import java.util.List;
import java.util.Map;

import static com.hedera.node.app.hapi.fees.apis.common.FeeConstants.MAX_KEYS;
import static com.hedera.node.app.hapi.fees.apis.common.FeeConstants.MIN_KEYS;

public class EntityCreate extends AbstractFeeModel {
    private final String service;
    private final String api;
    private final String description;
    private final int numFreeKeys;
    private final boolean customFeeCapable;

    private final List<ParameterDefinition> params = List.of(
            new ParameterDefinition("numKeys", "number", null, MIN_KEYS, MIN_KEYS, MAX_KEYS, "Number of keys")
    );
    private final List<ParameterDefinition> customFeeParams = List.of(
            new ParameterDefinition("hasCustomFee", "list", new Object[] {YesOrNo.YES, YesOrNo.NO}, YesOrNo.NO, 0, 0, "Enable custom fee?")
    );
    private final List<ParameterDefinition> hookParams = List.of(
            new ParameterDefinition("numHooksCreated", "number", null, 0, 0, 100, "Number of hooks created")
    );

    public EntityCreate(String service, String api, String description, int numFreeKeys, boolean customFeeCapable) {
        this.service = service;
        this.api = api;
        this.description = description;
        this.numFreeKeys = numFreeKeys;
        this.customFeeCapable = customFeeCapable;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    protected List<ParameterDefinition> apiSpecificParams() {
        List<ParameterDefinition> result = new java.util.ArrayList<>(params);
        if (customFeeCapable) {
            result.addAll(customFeeParams);
        }
        // Add hook parameters for CryptoCreate
        if ("CryptoCreate".equals(api)) {
            result.addAll(hookParams);
        }
        return result;
    }

    @Override
    protected FeeResult computeApiSpecificFee(Map<String, Object> values) {
        super.setNumFreeSignatures(numFreeKeys + 1); // The user needs to sign each of the keys to verify that they have the corresponding private key

        FeeResult fee = new FeeResult();
        if (customFeeCapable && values.get("hasCustomFee") == YesOrNo.YES) {
            fee.addDetail("Base fee", 1, BaseFeeRegistry.getBaseFee(api + "WithCustomFee"));
        } else {
            fee.addDetail("Base fee", 1, BaseFeeRegistry.getBaseFee(api));
        }

        int numKeys = (int) values.get("numKeys");

        if (numKeys > numFreeKeys) {
            fee.addDetail("Additional keys", numKeys - numFreeKeys, (numKeys - numFreeKeys) * BaseFeeRegistry.getBaseFee("PerKey"));
        }
        
        // Add hook creation fees for CryptoCreate
        if ("CryptoCreate".equals(api) && values.containsKey("numHooksCreated")) {
            int numHooksCreated = getInt(values.get("numHooksCreated"));
            if (numHooksCreated > 0) {
                fee.addDetail("Hook creation", numHooksCreated, numHooksCreated * BaseFeeRegistry.getBaseFee("HookCreate"));
            }
        }
        
        return fee;
    }
    
    private int getInt(Object value) {
        return (value instanceof Integer) ? (Integer) value : 0;
    }
}

