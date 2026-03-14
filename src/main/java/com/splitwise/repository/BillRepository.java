package com.splitwise.repository;

import com.splitwise.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByGroupIdOrderByBillDateDesc(Long groupId);
    List<Bill> findByPaidByIdOrderByBillDateDesc(Long userId);
}
