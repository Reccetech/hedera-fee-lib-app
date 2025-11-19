package com.hedera.node.app.hapi.fees.apis.hooks;

import com.hedera.node.app.hapi.fees.AbstractFeeModel;
import com.hedera.node.app.hapi.fees.BaseFeeRegistry;
import com.hedera.node.app.hapi.fees.FeeResult;
import com.hedera.node.app.hapi.fees.ParameterDefinition;

import java.util.List;
import java.util.Map;

public class LambdaSStore extends AbstractFeeModel {
    private final String service;
    private final String api;
    private final String description;

    private final List<ParameterDefinition> params = List.of(
            new ParameterDefinition("numStorageSlots", "number", null, 0, 0, 10000, "Number of storage slots written/deleted")
    );

    public LambdaSStore(String service, String api, String description) {
        this.service = service;
        this.api = api;
        this.description = description;
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
        return params;
    }

    @Override
    protected FeeResult computeApiSpecificFee(Map<String, Object> values) {
        FeeResult fee = new FeeResult();

        int numStorageSlots = getInt(values.get("numStorageSlots"));
        
        if (numStorageSlots > 0) {
            fee.addDetail("Storage slots", numStorageSlots, numStorageSlots * BaseFeeRegistry.getBaseFee("PerLambdaSStoreSlot"));
        } else {
            // Base fee is 0 if no slots, but we still show the transaction
            fee.addDetail("Base fee", 1, 0.0);
        }

        return fee;
    }

    private int getInt(Object value) {
        return (value instanceof Integer) ? (Integer) value : 0;
    }
}

