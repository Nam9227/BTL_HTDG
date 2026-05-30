package com.uet.common.network;

import java.io.Serializable;
import java.util.List;

public class AdminDashboardResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    // 4 số liệu trên các thẻ Card
    private int totalUsers;
    private int totalProducts;
    private int activeAuctions;
    private int pendingApprovals;

    // Danh sách lịch sử hiển thị ở bảng dưới
    private List<AdminActivity> recentActivities;

    public AdminDashboardResponse(int totalUsers, int totalProducts, int activeAuctions, int pendingApprovals, List<AdminActivity> recentActivities) {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.activeAuctions = activeAuctions;
        this.pendingApprovals = pendingApprovals;
        this.recentActivities = recentActivities;
    }

    // Gõ nhanh các hàm Getter/Setter ở đây...
    public int getTotalUsers() { return totalUsers; }
    public int getTotalProducts() { return totalProducts; }
    public int getActiveAuctions() { return activeAuctions; }
    public int getPendingApprovals() { return pendingApprovals; }
    public List<AdminActivity> getRecentActivities() { return recentActivities; }
}