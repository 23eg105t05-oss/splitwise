package com.splitwise.dto.request;

import com.splitwise.model.Bill;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BillRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    private Bill.SplitType splitType = Bill.SplitType.EQUAL;

    private Bill.Category category = Bill.Category.OTHER;

    private List<SplitDetailRequest> splitDetails;

    private LocalDateTime billDate;

    // Nested DTO
    public static class SplitDetailRequest {
        private Long userId;
        private BigDecimal amount;
        private Double percentage;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Bill.SplitType getSplitType() { return splitType; }
    public void setSplitType(Bill.SplitType splitType) { this.splitType = splitType; }

    public Bill.Category getCategory() { return category; }
    public void setCategory(Bill.Category category) { this.category = category; }

    public List<SplitDetailRequest> getSplitDetails() { return splitDetails; }
    public void setSplitDetails(List<SplitDetailRequest> splitDetails) { this.splitDetails = splitDetails; }

    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }
}
