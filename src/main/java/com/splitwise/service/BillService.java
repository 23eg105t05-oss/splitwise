package com.splitwise.service;

import com.splitwise.dto.request.BillRequest;
import com.splitwise.model.Bill;

import java.util.List;

public interface BillService {
    Bill createBill(BillRequest request, String payerEmail);
    List<Bill> getBillsByGroup(Long groupId);
    Bill getBillById(Long billId);
    void deleteBill(Long billId, String requestorEmail);
}
