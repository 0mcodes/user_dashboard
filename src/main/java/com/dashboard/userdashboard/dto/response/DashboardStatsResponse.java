package com.dashboard.userdashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalAdmins;
    private long totalManagers;
    private long totalRegularUsers;
    private long activeUsers;
    private long disabledUsers;
    private long lockedUsers;
    private long newUsersToday;
    private long newUsersThisWeek;
    private long newUsersThisMonth;
    private long totalAuditLogs;
    private long failedLoginsToday;
}