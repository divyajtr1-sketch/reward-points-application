package com.company.rewards.dto;

import java.time.LocalDate;
import java.util.List;

public record RewardResponse(
        LocalDate from,
        LocalDate to,
        List<CustomerReward> customers) {
}
