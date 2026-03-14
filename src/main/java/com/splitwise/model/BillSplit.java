package com.splitwise.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_splits")
public class BillSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    @JsonIgnoreProperties({"splitDetails", "group", "paidBy", "createdBy", "hibernateLazyInitializer"})
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"groups", "password", "hibernateLazyInitializer"})
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal share;

    @Column(name = "is_paid", nullable = false)
    private boolean paid = false;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getShare() { return share; }
    public void setShare(BigDecimal share) { this.share = share; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
