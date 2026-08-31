package com.example.transactionstarter.transaction.controller;

import com.example.transactionstarter.transaction.entity.Transaction;
import com.example.transactionstarter.transaction.enums.TransactionStatus;
import com.example.transactionstarter.transaction.enums.TransactionType;
import com.example.transactionstarter.transaction.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;


    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }


    // ---------------------------------------------------------
    // 1. CREATE TRANSACTION
    // ---------------------------------------------------------

    @Test
    void shouldCreateTransaction() throws Exception {

        String requestBody = """
                {
                    "transactionId": "TXN001",
                    "customerId": "CUST001",
                    "amount": 5000,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.transactionId", is("TXN001")))
        .andExpect(jsonPath("$.customerId", is("CUST001")))
        .andExpect(jsonPath("$.amount", is(5000)))
        .andExpect(jsonPath("$.currency", is("INR")))
        .andExpect(jsonPath("$.transactionType", is("PAYMENT")))
        .andExpect(jsonPath("$.status", is("PENDING")));
    }


    // ---------------------------------------------------------
    // 2. GET TRANSACTION
    // ---------------------------------------------------------

    @Test
    void shouldGetTransaction() throws Exception {

        Transaction transaction = new Transaction(
                "TXN002",
                "CUST002",
                new BigDecimal("2500"),
                "INR",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        transactionRepository.save(transaction);

        mockMvc.perform(
                get("/api/transactions/TXN002")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transactionId", is("TXN002")))
        .andExpect(jsonPath("$.customerId", is("CUST002")))
        .andExpect(jsonPath("$.amount", is(2500.0)))
        .andExpect(jsonPath("$.currency", is("INR")))
        .andExpect(jsonPath("$.transactionType", is("PAYMENT")))
        .andExpect(jsonPath("$.status", is("PENDING")));
    }


    // ---------------------------------------------------------
    // 3. UPDATE STATUS
    // ---------------------------------------------------------

    @Test
    void shouldUpdateTransactionStatus() throws Exception {

        Transaction transaction = new Transaction(
                "TXN003",
                "CUST003",
                new BigDecimal("3000"),
                "INR",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        transactionRepository.save(transaction);

        String requestBody = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(
                patch("/api/transactions/TXN003/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transactionId", is("TXN003")))
        .andExpect(jsonPath("$.status", is("COMPLETED")));
    }


    // ---------------------------------------------------------
    // 4. GET CUSTOMER TRANSACTIONS
    // ---------------------------------------------------------

    @Test
    void shouldGetCustomerTransactions() throws Exception {

        transactionRepository.save(
                new Transaction(
                        "TXN004",
                        "CUST004",
                        new BigDecimal("1000"),
                        "INR",
                        TransactionType.PAYMENT,
                        TransactionStatus.PENDING
                )
        );

        transactionRepository.save(
                new Transaction(
                        "TXN005",
                        "CUST004",
                        new BigDecimal("2000"),
                        "INR",
                        TransactionType.PAYMENT,
                        TransactionStatus.PENDING
                )
        );

        mockMvc.perform(
                get("/api/customers/CUST004/transactions")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
    }


    // ---------------------------------------------------------
    // 5. VALIDATION TEST
    // Amount greater than 100000 should return 400
    // ---------------------------------------------------------

    @Test
    void shouldRejectTransactionWhenAmountExceedsLimit() throws Exception {

        String requestBody = """
                {
                    "transactionId": "TXN006",
                    "customerId": "CUST006",
                    "amount": 150000,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }


    // ---------------------------------------------------------
    // 6. DUPLICATE TRANSACTION ID TEST
    // Duplicate ID should return 400
    // ---------------------------------------------------------

    @Test
    void shouldRejectDuplicateTransactionId() throws Exception {

        Transaction transaction = new Transaction(
                "TXN007",
                "CUST007",
                new BigDecimal("5000"),
                "INR",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        transactionRepository.save(transaction);

        String requestBody = """
                {
                    "transactionId": "TXN007",
                    "customerId": "CUST007",
                    "amount": 5000,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isConflict());
    }


    // ---------------------------------------------------------
    // 7. NON-EXISTENT TRANSACTION TEST
    // Transaction not found should return 404
    // ---------------------------------------------------------

    @Test
    void shouldRejectWhenTransactionDoesNotExist() throws Exception {

        mockMvc.perform(
                get("/api/transactions/TXN999")
        )
        .andExpect(status().isNotFound());
    }
}