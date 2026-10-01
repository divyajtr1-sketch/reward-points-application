package com.company.rewards.controllertest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.rewards.entity.Customer;
import com.company.rewards.entity.Transaction;
import com.company.rewards.repository.CustomerRepository;
import com.company.rewards.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class RewardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    public void setUp() {
        transactionRepository.deleteAll();
        customerRepository.deleteAll();

        Customer alice = customerRepository.save(new Customer("Alice"));
        Customer bob = customerRepository.save(new Customer("Bob"));

        transactionRepository.save(new Transaction(
                alice, new BigDecimal("120"), LocalDate.of(2026, 7, 10)));
        transactionRepository.save(new Transaction(
                alice, new BigDecimal("75"), LocalDate.of(2026, 8, 12)));
        transactionRepository.save(new Transaction(
                alice, new BigDecimal("40"), LocalDate.of(2026, 9, 15)));

        transactionRepository.save(new Transaction(
                bob, new BigDecimal("200"), LocalDate.of(2026, 7, 5)));
        transactionRepository.save(new Transaction(
                bob, new BigDecimal("50"), LocalDate.of(2026, 8, 18)));
        transactionRepository.save(new Transaction(
                bob, new BigDecimal("150"), LocalDate.of(2026, 9, 20)));
    }

    @Test
    public void shouldReturnMonthlyAndTotalRewards() throws Exception {
        mockMvc.perform(get("/api/rewards")
                        .param("from", "2026-07-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers", hasSize(2)))
                .andExpect(jsonPath("$.customers[0].customerName", is("Alice")))
                .andExpect(jsonPath("$.customers[0].monthlyRewards[0].month", is("2026-07")))
                .andExpect(jsonPath("$.customers[0].monthlyRewards[0].points", is(90)))
                .andExpect(jsonPath("$.customers[0].monthlyRewards[1].points", is(25)))
                .andExpect(jsonPath("$.customers[0].monthlyRewards[2].points", is(0)))
                .andExpect(jsonPath("$.customers[0].totalPoints", is(115)))
                .andExpect(jsonPath("$.customers[1].totalPoints", is(400)));
    }

    @Test
    public void shouldReturnBadRequestForInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/rewards")
                        .param("from", "2026-09-30")
                        .param("to", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("'from' date cannot be after 'to' date")));
    }

    @Test
    public void shouldReturnNotFoundForUnknownCustomer() throws Exception {
        mockMvc.perform(get("/api/rewards/999999")
                        .param("from", "2026-07-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Customer not found: 999999")));
    }
}
