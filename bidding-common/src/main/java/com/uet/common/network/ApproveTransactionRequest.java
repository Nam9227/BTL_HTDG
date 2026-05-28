package com.uet.common.network;

import java.io.Serializable;

public class ApproveTransactionRequest implements Serializable {
    private long transactionId;

    public ApproveTransactionRequest(long transactionId) {

        this.transactionId = transactionId;
    }

    public long getTransactionId() {

        return transactionId;
    }
}
