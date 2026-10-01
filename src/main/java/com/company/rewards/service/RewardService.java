package com.company.rewards.service;

import com.company.rewards.dto.CustomerReward;
import com.company.rewards.dto.MonthlyReward;
import com.company.rewards.dto.RewardResponse;
import com.company.rewards.entity.Customer;
import com.company.rewards.entity.Transaction;
import com.company.rewards.exception.CustomerNotFoundException;
import com.company.rewards.exception.InvalidDateRangeException;
import com.company.rewards.repository.CustomerRepository;
import com.company.rewards.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RewardService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public RewardService(CustomerRepository customerRepository,
                         TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    public RewardResponse calculateRewards(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(from, to);
        Map<Long, List<Transaction>> byCustomer = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        transaction -> transaction.getCustomer().getId(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));

        List<CustomerReward> rewards = customerRepository.findAll().stream()
                .sorted(Comparator.comparing(Customer::getId))
                .map(customer -> buildCustomerReward(customer.getId(), customer.getName(),
                        byCustomer.getOrDefault(customer.getId(), List.of()), from, to))
                .toList();

        return new RewardResponse(from, to, rewards);
    }

    public CustomerReward calculateCustomerRewards(Long customerId, LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        List<Transaction> transactions = transactionRepository
                .findByCustomerIdAndTransactionDateBetween(customerId, from, to);

        return buildCustomerReward(customer.getId(), customer.getName(), transactions, from, to);
    }

    private CustomerReward buildCustomerReward(Long customerId,
                                                String customerName,
                                                List<Transaction> transactions,
                                                LocalDate from,
                                                LocalDate to) {
        Map<YearMonth, Long> pointsByMonth = new LinkedHashMap<>();
        YearMonth firstMonth = YearMonth.from(from);
        YearMonth lastMonth = YearMonth.from(to);

        // Months are generated from the requested date range; no month is hard-coded.
        for (YearMonth month = firstMonth; !month.isAfter(lastMonth); month = month.plusMonths(1)) {
            pointsByMonth.put(month, 0L);
        }

        for (Transaction transaction : transactions) {
            YearMonth month = YearMonth.from(transaction.getTransactionDate());
            long points = calculatePoints(transaction.getAmount());
            pointsByMonth.computeIfPresent(month, (key, value) -> value + points);
        }

        List<MonthlyReward> monthlyRewards = pointsByMonth.entrySet().stream()
                .map(entry -> new MonthlyReward(entry.getKey().toString(), entry.getValue()))
                .toList();

        long totalPoints = monthlyRewards.stream()
                .mapToLong(MonthlyReward::points)
                .sum();

        return new CustomerReward(customerId, customerName, monthlyRewards, totalPoints);
    }

    public long calculatePoints(java.math.BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Transaction amount must be zero or positive");
        }

        long dollars = amount.longValue();
        if (dollars <= 50) {
            return 0;
        }
        if (dollars <= 100) {
            return dollars - 50;
        }
        return 50 + ((dollars - 100) * 2);
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidDateRangeException("Both 'from' and 'to' dates are required");
        }
        if (from.isAfter(to)) {
            throw new InvalidDateRangeException("'from' date cannot be after 'to' date");
        }
    }
}
