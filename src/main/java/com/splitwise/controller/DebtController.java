package com.splitwise.controller;

import com.splitwise.dto.request.SettleDebtRequest;
import com.splitwise.dto.response.ApiResponse;
import com.splitwise.dto.response.SimplifiedDebtResponse;
import com.splitwise.model.Debt;
import com.splitwise.model.Transaction;
import com.splitwise.service.DebtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    // GET /api/debts/me?groupId=
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Debt>>> getMyDebts(
            @RequestParam(required = false) Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Debt> debts = debtService.getMyDebts(userDetails.getUsername(), groupId);
        return ResponseEntity.ok(ApiResponse.ok(debts));
    }

    // GET /api/debts/group/{groupId}
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByGroup(@PathVariable Long groupId) {
        List<Debt> debts = debtService.getDebtsByGroup(groupId);
        return ResponseEntity.ok(ApiResponse.ok(debts));
    }

    // GET /api/debts/group/{groupId}/simplified
    @GetMapping("/group/{groupId}/simplified")
    public ResponseEntity<ApiResponse<List<SimplifiedDebtResponse>>> getSimplifiedDebts(
            @PathVariable Long groupId) {
        List<SimplifiedDebtResponse> simplified = debtService.getSimplifiedDebts(groupId);
        return ResponseEntity.ok(ApiResponse.ok(simplified));
    }

    // POST /api/debts/{id}/settle
    @PostMapping("/{id}/settle")
    public ResponseEntity<ApiResponse<Transaction>> settleDebt(
            @PathVariable Long id,
            @RequestBody(required = false) SettleDebtRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String method = (request != null && request.getMethod() != null)
                ? request.getMethod().name() : "OTHER";
        String note = (request != null) ? request.getNote() : "";
        Transaction transaction = debtService.settleDebt(id, userDetails.getUsername(), method, note);
        return ResponseEntity.ok(ApiResponse.ok("Debt settled successfully.", transaction));
    }
}
