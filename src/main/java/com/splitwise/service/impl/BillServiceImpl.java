package com.splitwise.service.impl;

import com.splitwise.dto.request.BillRequest;
import com.splitwise.exception.BadRequestException;
import com.splitwise.exception.ResourceNotFoundException;
import com.splitwise.exception.UnauthorizedException;
import com.splitwise.model.*;
import com.splitwise.repository.*;
import com.splitwise.service.BillService;
import com.splitwise.util.SplitAlgorithmUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final DebtRepository debtRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public BillServiceImpl(BillRepository billRepository,
                           DebtRepository debtRepository,
                           GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           UserRepository userRepository) {
        this.billRepository = billRepository;
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Bill createBill(BillRequest request, String payerEmail) {
        User payer = userRepository.findByEmail(payerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Group group = groupRepository.findById(request.getGroupId())
            .orElseThrow(() -> new ResourceNotFoundException("Group not found."));

        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(group.getId());
        List<Long> memberIds = groupMembers.stream()
            .map(m -> m.getUser().getId()).collect(Collectors.toList());

        Map<Long, User> userMap = groupMembers.stream()
            .collect(Collectors.toMap(m -> m.getUser().getId(), GroupMember::getUser));

        // Build split details
        List<BillSplit> splits = buildSplits(request, memberIds, userMap);

        Bill bill = new Bill();
        bill.setTitle(request.getTitle());
        bill.setDescription(request.getDescription());
        bill.setAmount(request.getAmount());
        bill.setCurrency(request.getCurrency() != null ? request.getCurrency() : group.getCurrency());
        bill.setGroup(group);
        bill.setPaidBy(payer);
        bill.setSplitType(request.getSplitType() != null ? request.getSplitType() : Bill.SplitType.EQUAL);
        bill.setCategory(request.getCategory() != null ? request.getCategory() : Bill.Category.OTHER);
        bill.setBillDate(request.getBillDate());
        bill.setCreatedBy(payer);

        for (BillSplit split : splits) {
            split.setBill(bill);
        }
        bill.setSplitDetails(splits);

        Bill saved = billRepository.save(bill);

        // Create debt records for non-payers
        for (BillSplit split : splits) {
            if (!split.getUser().getId().equals(payer.getId())) {
                Debt debt = new Debt();
                debt.setFromUser(split.getUser());
                debt.setToUser(payer);
                debt.setAmount(split.getShare());
                debt.setCurrency(saved.getCurrency());
                debt.setGroup(group);
                debt.setBill(saved);
                debtRepository.save(debt);
            }
        }

        return saved;
    }

    @Override
    public List<Bill> getBillsByGroup(Long groupId) {
        return billRepository.findByGroupIdOrderByBillDateDesc(groupId);
    }

    @Override
    public Bill getBillById(Long billId) {
        return billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
    }

    @Override
    @Transactional
    public void deleteBill(Long billId, String requestorEmail) {
        Bill bill = getBillById(billId);
        User requestor = userRepository.findByEmail(requestorEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!bill.getCreatedBy().getId().equals(requestor.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this bill.");
        }
        billRepository.delete(bill);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<BillSplit> buildSplits(BillRequest request,
                                        List<Long> memberIds,
                                        Map<Long, User> userMap) {
        Bill.SplitType splitType = request.getSplitType() != null ? request.getSplitType() : Bill.SplitType.EQUAL;
        List<BillSplit> splits = new ArrayList<>();

        if (splitType == Bill.SplitType.EQUAL) {
            Map<Long, BigDecimal> shares = SplitAlgorithmUtil.splitEqually(request.getAmount(), memberIds);
            shares.forEach((uid, share) -> {
                BillSplit bs = new BillSplit();
                bs.setUser(userMap.get(uid));
                bs.setShare(share);
                splits.add(bs);
            });

        } else if (splitType == Bill.SplitType.EXACT) {
            if (request.getSplitDetails() == null || request.getSplitDetails().isEmpty()) {
                throw new BadRequestException("splitDetails required for EXACT split.");
            }
            BigDecimal total = request.getSplitDetails().stream()
                .map(BillRequest.SplitDetailRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (total.compareTo(request.getAmount()) != 0) {
                throw new BadRequestException("Exact amounts must add up to the total bill amount.");
            }
            for (BillRequest.SplitDetailRequest detail : request.getSplitDetails()) {
                User user = userMap.get(detail.getUserId());
                if (user == null) throw new BadRequestException("User " + detail.getUserId() + " is not a group member.");
                BillSplit bs = new BillSplit();
                bs.setUser(user);
                bs.setShare(detail.getAmount());
                splits.add(bs);
            }

        } else if (splitType == Bill.SplitType.PERCENTAGE) {
            if (request.getSplitDetails() == null || request.getSplitDetails().isEmpty()) {
                throw new BadRequestException("splitDetails required for PERCENTAGE split.");
            }
            double totalPct = request.getSplitDetails().stream()
                .mapToDouble(BillRequest.SplitDetailRequest::getPercentage).sum();
            if (Math.abs(totalPct - 100.0) > 0.01) {
                throw new BadRequestException("Percentages must add up to 100.");
            }
            for (BillRequest.SplitDetailRequest detail : request.getSplitDetails()) {
                User user = userMap.get(detail.getUserId());
                if (user == null) throw new BadRequestException("User " + detail.getUserId() + " is not a group member.");
                BigDecimal share = request.getAmount()
                    .multiply(BigDecimal.valueOf(detail.getPercentage() / 100))
                    .setScale(2, RoundingMode.HALF_UP);
                BillSplit bs = new BillSplit();
                bs.setUser(user);
                bs.setShare(share);
                splits.add(bs);
            }
        }

        return splits;
    }
}
