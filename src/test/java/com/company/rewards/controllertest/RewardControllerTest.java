package com.company.rewards.controllertest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.rewards.controller.GlobalExceptionHandler;
import com.company.rewards.controller.RewardController;
import com.company.rewards.dto.CustomerReward;
import com.company.rewards.dto.MonthlyReward;
import com.company.rewards.dto.RewardResponse;
import com.company.rewards.exception.CustomerNotFoundException;
import com.company.rewards.exception.InvalidDateRangeException;
import com.company.rewards.service.RewardService;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RewardController.class)
@Import(GlobalExceptionHandler.class)
public class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardService rewardService;

    @Test
    public void ReturnRewardsForAllCustomers() throws Exception {
        RewardResponse response = new RewardResponse(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 9, 30),
                List.of(new CustomerReward(
                        1L,
                        "Alice",
                        List.of(
                                new MonthlyReward("2026-07", 90),
                                new MonthlyReward("2026-08", 25),
                                new MonthlyReward("2026-09", 0)),
                        115)));

        when(rewardService.calculateRewards(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(response);

        mockMvc.perform(get("/api/rewards")
                        .param("from", "2026-07-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers[0].customerId").value(1))
                .andExpect(jsonPath("$.customers[0].customerName").value("Alice"))
                .andExpect(jsonPath("$.customers[0].monthlyRewards[0].points").value(90))
                .andExpect(jsonPath("$.customers[0].totalPoints").value(115));
    }

    @Test
    public void ReturnRewardsForSingleCustomer() throws Exception {
        CustomerReward response = new CustomerReward(
                1L,
                "Alice",
                List.of(new MonthlyReward("2026-07", 90)),
                90);

        when(rewardService.calculateCustomerRewards(
                1L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)))
                .thenReturn(response);

        mockMvc.perform(get("/api/rewards/1")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.totalPoints").value(90));
    }

    @Test
    public void ReturnBadRequestWhenServiceRejectsDateRange() throws Exception {
        when(rewardService.calculateRewards(
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 7, 1)))
                .thenThrow(new InvalidDateRangeException("'from' date cannot be after 'to' date"));

        mockMvc.perform(get("/api/rewards")
                        .param("from", "2026-09-30")
                        .param("to", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("'from' date cannot be after 'to' date"));
    }

    @Test
    public void ReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(rewardService.calculateCustomerRewards(
                999L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30)))
                .thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(get("/api/rewards/999")
                        .param("from", "2026-07-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found: 999"));
    }

    @Test
    public void ReturnBadRequestWhenDateParameterIsMissing() throws Exception {
        mockMvc.perform(get("/api/rewards")
                        .param("from", "2026-07-01"))
                .andExpect(status().isBadRequest());
    }
}
