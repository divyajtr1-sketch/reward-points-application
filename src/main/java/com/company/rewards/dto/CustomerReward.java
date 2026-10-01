package com.company.rewards.dto;

import java.util.List;

public record CustomerReward(
        Long customerId,
        String customerName,
        List<MonthlyReward> monthlyRewards,
        long totalPoints) {
}
