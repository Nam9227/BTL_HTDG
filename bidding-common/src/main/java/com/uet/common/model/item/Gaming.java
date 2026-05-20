package com.uet.common.model.item;

public class Gaming extends Item {

    private String platform;
    private String condition;

    public Gaming() {
    }

    public Gaming(String platform, String condition) {
        this.platform = platform;
        this.condition = condition;
    }

    public String getPlatform() {
        return platform;
    }

    public String getCondition() {
        return condition;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
    @Override
    public String getCategory() {
        return "GAMING";
    }
    
}