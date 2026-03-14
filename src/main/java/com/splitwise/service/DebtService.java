package com.splitwise.service;

import com.splitwise.dto.response.SimplifiedDebtResponse;
import com.splitwise.model.Debt;
import com.splitwise.model.Transaction;

import java.util.List;

public interface DebtService {
    List<Debt> getDebtsByGroup(Long groupId);
    List<Debt> getMyDebts(String email, Long groupId);
    Transaction settleDebt(Long debtId, String requestorEmail, String method, String note);
    List<SimplifiedDebtResponse> getSimplifiedDebts(Long groupId);
}
