package com.example.transactionstarter.transaction.service;

import com.example.transactionstarter.transaction.entity.Transaction;
import com.example.transactionstarter.transaction.enums.TransactionStatus;
import com.example.transactionstarter.transaction.repository.TransactionRepository;
import com.example.transactionstarter.transaction.exception.DuplicateTransactionException;
import com.example.transactionstarter.transaction.exception.InvalidTransactionException;
import com.example.transactionstarter.transaction.exception.TransactionNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private static final BigDecimal MAX_TRANSACTION_AMOUNT =
            new BigDecimal("100000");

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(Transaction transaction) {

        validateTransaction(transaction);

        if (transactionRepository.existsById(transaction.getTransactionId())) {
            throw new DuplicateTransactionException(
                    "Transaction ID already exists: "
                            + transaction.getTransactionId()
            );
        }

        transaction.setStatus(TransactionStatus.PENDING);

        return transactionRepository.save(transaction);
    }

    public Transaction getTransaction(String transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found: " + transactionId
                ));
    }

    public Transaction updateStatus(String transactionId,
                                    TransactionStatus newStatus) {

        Transaction transaction = getTransaction(transactionId);

        validateStatusTransition(transaction.getStatus(), newStatus);

        transaction.setStatus(newStatus);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getCustomerTransactions(String customerId) {

        if (customerId == null || customerId.isBlank()) {
            throw new InvalidTransactionException(
                    "Customer ID is required"
            );
        }

        return transactionRepository.findByCustomerId(customerId);
    }

    private void validateTransaction(Transaction transaction) {

        if (transaction == null) {
            throw new InvalidTransactionException(
                    "Transaction is required"
            );
        }

        if (transaction.getTransactionId() == null ||
                transaction.getTransactionId().isBlank()) {
            throw new InvalidTransactionException(
                    "Transaction ID is required"
            );
        }

        if (transaction.getCustomerId() == null ||
                transaction.getCustomerId().isBlank()) {
            throw new InvalidTransactionException(
                    "Customer ID is required"
            );
        }

        if (transaction.getAmount() == null) {
            throw new InvalidTransactionException(
                    "Amount is required"
            );
        }

        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Amount must be greater than zero"
            );
        }

        if (transaction.getAmount().compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new InvalidTransactionException(
                    "Amount must not exceed 100000"
            );
        }

        if (transaction.getCurrency() == null ||
                transaction.getCurrency().isBlank()) {
            throw new InvalidTransactionException(
                    "Currency is required"
            );
        }

        if (!isSupportedCurrency(transaction.getCurrency())) {
            throw new InvalidTransactionException(
                    "Unsupported currency: " + transaction.getCurrency()
            );
        }

        if (transaction.getTransactionType() == null) {
            throw new InvalidTransactionException(
                    "Transaction type is required"
            );
        }
    }

    private boolean isSupportedCurrency(String currency) {

        return currency.equalsIgnoreCase("INR")
                || currency.equalsIgnoreCase("USD")
                || currency.equalsIgnoreCase("EUR");
    }

    private void validateStatusTransition(TransactionStatus currentStatus,
                                          TransactionStatus newStatus) {

        if (newStatus == null) {
            throw new InvalidTransactionException(
                    "New status is required"
            );
        }

        if (currentStatus == null) {
            throw new InvalidTransactionException(
                    "Current transaction status is missing"
            );
        }

        if (currentStatus == TransactionStatus.COMPLETED &&
                newStatus != TransactionStatus.COMPLETED) {

            throw new InvalidTransactionException(
                    "Completed transaction cannot change status"
            );
        }

        if (currentStatus == TransactionStatus.FAILED &&
                newStatus == TransactionStatus.COMPLETED) {

            throw new InvalidTransactionException(
                    "Failed transaction cannot be changed to COMPLETED"
            );
        }

        if (currentStatus == TransactionStatus.PENDING &&
                newStatus == TransactionStatus.PENDING) {

            throw new InvalidTransactionException(
                    "Transaction is already PENDING"
            );
        }
    }
}