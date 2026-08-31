package com.example.transactionstarter.transaction.dto;

import com.example.transactionstarter.transaction.enums.TransactionStatus;

public class UpdateStatusRequest {

    private TransactionStatus status;

    public UpdateStatusRequest() {
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}