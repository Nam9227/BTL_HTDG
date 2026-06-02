package com.uet.common.network;

import java.io.Serializable;

public class AdminActivity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String time;
    private String action;
    private String target;
    private String status;

    public AdminActivity(String time, String action, String target, String status) {
        this.time = time;
        this.action = action;
        this.target = target;
        this.status = status;
    }

    
    public String getTime() { return time; }
    public String getAction() { return action; }
    public String getTarget() { return target; }
    public String getStatus() { return status; }
}