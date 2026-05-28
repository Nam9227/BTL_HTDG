package com.uet.client.model.dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DashboardActivity {
    private final StringProperty time;
    private final StringProperty action;
    private final StringProperty target;
    private final StringProperty status;

    public DashboardActivity(String time, String action, String target, String status) {
        this.time = new SimpleStringProperty(time);
        this.action = new SimpleStringProperty(action);
        this.target = new SimpleStringProperty(target);
        this.status = new SimpleStringProperty(status);
    }
    public StringProperty timeProperty() { return time; }
    public StringProperty actionProperty() { return action; }
    public StringProperty targetProperty() { return target; }
    public StringProperty statusProperty() { return status; }
    public String getTime() { return time.get(); }
    public String getAction() { return action.get(); }
    public String getTarget() { return target.get(); }
    public String getStatus() { return status.get(); }
}