package com.uet.common.network;

import java.io.Serializable;

public class RejectTransactionRequest implements Serializable {
    private long transactionId;

    public RejectTransactionRequest(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getTransactionId() {
        return transactionId;
    }
}
