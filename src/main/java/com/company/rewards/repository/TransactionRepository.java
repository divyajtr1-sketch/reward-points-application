package com.company.rewards.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.rewards.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTransactionDateBetween(LocalDate from, LocalDate to);

    List<Transaction> findByCustomerIdAndTransactionDateBetween(Long customerId, LocalDate from, LocalDate to);
}
