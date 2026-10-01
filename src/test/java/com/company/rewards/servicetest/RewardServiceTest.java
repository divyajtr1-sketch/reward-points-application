package com.company.rewards.servicetest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.rewards.dto.CustomerReward;
import com.company.rewards.dto.RewardResponse;
import com.company.rewards.entity.Customer;
import com.company.rewards.entity.Transaction;
import com.company.rewards.exception.CustomerNotFoundException;
import com.company.rewards.exception.InvalidDateRangeException;
import com.company.rewards.repository.CustomerRepository;
import com.company.rewards.repository.TransactionRepository;
import com.company.rewards.service.RewardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardService rewardService;

    private Customer alice;
    private Customer bob;

    @BeforeEach
    public void setUp() {
        alice = new Customer("Alice");
        bob = new Customer("Bob");
        ReflectionTestUtils.setField(alice, "id", 1L);
        ReflectionTestUtils.setField(bob, "id", 2L);
    }

    @Test
    public void shouldCalculateZeroPointsFor50OrLess() {
        assertEquals(0, rewardService.calculatePoints(new BigDecimal("50")));
        assertEquals(0, rewardService.calculatePoints(new BigDecimal("40")));
    }

    @Test
    public void shouldCalculateOnePointPerDollarBetween50And100() {
        assertEquals(25, rewardService.calculatePoints(new BigDecimal("75")));
        assertEquals(50, rewardService.calculatePoints(new BigDecimal("100")));
    }

    @Test
    public void shouldCalculateTwoPointsPerDollarAbove100() {
        assertEquals(90, rewardService.calculatePoints(new BigDecimal("120")));
        assertEquals(250, rewardService.calculatePoints(new BigDecimal("200")));
    }

    @Test
    public void shouldIgnoreFractionalDollarForPoints() {
        assertEquals(25, rewardService.calculatePoints(new BigDecimal("75.99")));
    }

    @Test
    public void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardService.calculatePoints(new BigDecimal("-1")));
    }

    @Test
    public void shouldRejectNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardService.calculatePoints(null));
    }

    @Test
    public void shouldCalculateRewardsForAllCustomersAcrossRequestedMonths() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);

        List<Transaction> transactions = List.of(
                new Transaction(alice, new BigDecimal("120"), LocalDate.of(2026, 7, 10)),
                new Transaction(alice, new BigDecimal("75"), LocalDate.of(2026, 8, 12)),
                new Transaction(alice, new BigDecimal("40"), LocalDate.of(2026, 9, 15)),
                new Transaction(bob, new BigDecimal("200"), LocalDate.of(2026, 7, 5)));

        when(transactionRepository.findByTransactionDateBetween(from, to)).thenReturn(transactions);
        when(customerRepository.findAll()).thenReturn(List.of(alice, bob));

        RewardResponse response = rewardService.calculateRewards(from, to);

        assertEquals(2, response.customers().size());
        assertEquals("Alice", response.customers().get(0).customerName());
        assertEquals(90, response.customers().get(0).monthlyRewards().get(0).points());
        assertEquals(25, response.customers().get(0).monthlyRewards().get(1).points());
        assertEquals(0, response.customers().get(0).monthlyRewards().get(2).points());
        assertEquals(115, response.customers().get(0).totalPoints());
        assertEquals(250, response.customers().get(1).totalPoints());

        verify(transactionRepository).findByTransactionDateBetween(from, to);
        verify(customerRepository).findAll();
    }

    @Test
    public void shouldCalculateRewardsForSingleCustomer() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(1L, from, to))
                .thenReturn(List.of(
                        new Transaction(alice, new BigDecimal("120"), LocalDate.of(2026, 7, 10)),
                        new Transaction(alice, new BigDecimal("75"), LocalDate.of(2026, 8, 12))));

        CustomerReward reward = rewardService.calculateCustomerRewards(1L, from, to);

        assertEquals(1L, reward.customerId());
        assertEquals("Alice", reward.customerName());
        assertEquals(90, reward.monthlyRewards().get(0).points());
        assertEquals(25, reward.monthlyRewards().get(1).points());
        assertEquals(0, reward.monthlyRewards().get(2).points());
        assertEquals(115, reward.totalPoints());
    }

    @Test
    public void shouldThrowCustomerNotFoundException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> rewardService.calculateCustomerRewards(
                999L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30)));
    }

    @Test
    public void shouldRejectInvalidDateRange() {
        LocalDate from = LocalDate.of(2026, 9, 30);
        LocalDate to = LocalDate.of(2026, 7, 1);

        assertThrows(InvalidDateRangeException.class,
                () -> rewardService.calculateRewards(from, to));
    }

    @Test
    public void shouldRejectNullDates() {
        assertThrows(InvalidDateRangeException.class,
                () -> rewardService.calculateRewards(null, LocalDate.of(2026, 9, 30)));
    }
}
