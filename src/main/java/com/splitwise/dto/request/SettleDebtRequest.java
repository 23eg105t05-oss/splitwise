package com.splitwise.dto.request;

import com.splitwise.model.Transaction;

public class SettleDebtRequest {

    private Transaction.Method method = Transaction.Method.OTHER;
    private String note = "";

    public Transaction.Method getMethod() { return method; }
    public void setMethod(Transaction.Method method) { this.method = method; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
