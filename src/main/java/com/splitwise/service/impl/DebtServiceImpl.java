package com.splitwise.service.impl;

import com.splitwise.dto.response.SimplifiedDebtResponse;
import com.splitwise.exception.BadRequestException;
import com.splitwise.exception.ResourceNotFoundException;
import com.splitwise.model.Debt;
import com.splitwise.model.Transaction;
import com.splitwise.model.User;
import com.splitwise.repository.DebtRepository;
import com.splitwise.repository.TransactionRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.service.DebtService;
import com.splitwise.util.SplitAlgorithmUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public DebtServiceImpl(DebtRepository debtRepository,
                           TransactionRepository transactionRepository,
                           UserRepository userRepository) {
        this.debtRepository = debtRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Debt> getDebtsByGroup(Long groupId) {
        return debtRepository.findByGroupIdAndSettledFalse(groupId);
    }

    @Override
    public List<Debt> getMyDebts(String email, Long groupId) {
        User user = getUser(email);
        if (groupId != null) {
            return debtRepository.findUnsettledDebtsByUserAndGroup(user.getId(), groupId);
        }
        return debtRepository.findUnsettledDebtsByUser(user.getId());
    }

    @Override
    @Transactional
    public Transaction settleDebt(Long debtId, String requestorEmail, String method, String note) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with id: " + debtId));

        if (debt.isSettled()) {
            throw new BadRequestException("Debt is already settled.");
        }

        User recordedBy = getUser(requestorEmail);

        Transaction.Method paymentMethod;
        try {
            paymentMethod = (method != null) ? Transaction.Method.valueOf(method.toUpperCase()) : Transaction.Method.OTHER;
        } catch (IllegalArgumentException e) {
            paymentMethod = Transaction.Method.OTHER;
        }

        Transaction transaction = new Transaction();
        transaction.setFromUser(debt.getFromUser());
        transaction.setToUser(debt.getToUser());
        transaction.setAmount(debt.getAmount());
        transaction.setCurrency(debt.getCurrency());
        transaction.setGroup(debt.getGroup());
        transaction.setNote(note != null ? note : "");
        transaction.setMethod(paymentMethod);
        transaction.setStatus(Transaction.Status.COMPLETED);
        transaction.setRecordedBy(recordedBy);
        transaction.getSettledDebts().add(debt);
        transactionRepository.save(transaction);

        debt.setSettled(true);
        debt.setSettledAt(LocalDateTime.now());
        debtRepository.save(debt);

        return transaction;
    }

    @Override
    public List<SimplifiedDebtResponse> getSimplifiedDebts(Long groupId) {
        List<Debt> debts = debtRepository.findByGroupIdAndSettledFalse(groupId);
        return SplitAlgorithmUtil.simplifyDebts(debts);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
