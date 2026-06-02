package com.uet.common.network;

import com.uet.common.model.auction.AutoBid;
import java.io.Serializable;

public class GetAutoBidResponse implements Serializable {
    private boolean success;
    private AutoBid autoBid;

    public GetAutoBidResponse(boolean success, AutoBid autoBid) {
        this.success = success;
        this.autoBid = autoBid;
    }

    public boolean isSuccess() { return success; }
    public AutoBid getAutoBid() { return autoBid; }
}
