package com.splitwise.controller;

import com.splitwise.dto.request.BillRequest;
import com.splitwise.dto.response.ApiResponse;
import com.splitwise.model.Bill;
import com.splitwise.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    // POST /api/bills
    @PostMapping
    public ResponseEntity<ApiResponse<Bill>> createBill(
            @Valid @RequestBody BillRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Bill bill = billService.createBill(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bill created successfully.", bill));
    }

    // GET /api/bills/group/{groupId}
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<Bill>>> getBillsByGroup(
            @PathVariable Long groupId) {
        List<Bill> bills = billService.getBillsByGroup(groupId);
        return ResponseEntity.ok(ApiResponse.ok(bills));
    }

    // GET /api/bills/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Bill>> getBillById(@PathVariable Long id) {
        Bill bill = billService.getBillById(id);
        return ResponseEntity.ok(ApiResponse.ok(bill));
    }

    // DELETE /api/bills/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBill(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        billService.deleteBill(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Bill deleted successfully.", null));
    }
}
