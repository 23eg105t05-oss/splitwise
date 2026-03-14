package com.splitwise.util;

import com.splitwise.dto.response.SimplifiedDebtResponse;
import com.splitwise.model.Debt;
import com.splitwise.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class SplitAlgorithmUtil {

    /**
     * Simplify debts using a greedy net-balance algorithm.
     * Reduces the number of transactions required to settle all debts.
     */
    public static List<SimplifiedDebtResponse> simplifyDebts(List<Debt> debts) {
        Map<Long, BigDecimal> balance = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();

        for (Debt debt : debts) {
            User from = debt.getFromUser();
            User to = debt.getToUser();
            userMap.put(from.getId(), from);
            userMap.put(to.getId(), to);

            balance.merge(from.getId(), debt.getAmount().negate(), BigDecimal::add);
            balance.merge(to.getId(), debt.getAmount(), BigDecimal::add);
        }

        List<long[]> creditorList = new ArrayList<>(); // [id, amount*100]
        List<long[]> debtorList = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : balance.entrySet()) {
            long scaled = entry.getValue().setScale(2, RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100)).longValue();
            if (scaled > 0) creditorList.add(new long[]{entry.getKey(), scaled});
            else if (scaled < 0) debtorList.add(new long[]{entry.getKey(), -scaled});
        }

        List<SimplifiedDebtResponse> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < creditorList.size() && j < debtorList.size()) {
            long[] creditor = creditorList.get(i);
            long[] debtor = debtorList.get(j);
            long settled = Math.min(creditor[1], debtor[1]);

            BigDecimal amount = BigDecimal.valueOf(settled).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            User fromUser = userMap.get(debtor[0]);
            User toUser = userMap.get(creditor[0]);

            result.add(new SimplifiedDebtResponse(
                fromUser.getId(), fromUser.getName(),
                toUser.getId(), toUser.getName(),
                amount
            ));

            creditor[1] -= settled;
            debtor[1] -= settled;
            if (creditor[1] == 0) i++;
            if (debtor[1] == 0) j++;
        }

        return result;
    }

    /**
     * Split amount equally among member IDs.
     */
    public static Map<Long, BigDecimal> splitEqually(BigDecimal totalAmount, List<Long> memberIds) {
        BigDecimal share = totalAmount.divide(
            BigDecimal.valueOf(memberIds.size()), 2, RoundingMode.HALF_UP);
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        memberIds.forEach(id -> result.put(id, share));
        return result;
    }
}
