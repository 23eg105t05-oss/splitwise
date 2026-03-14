package com.splitwise.repository;

import com.splitwise.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByGroupIdAndSettledFalse(Long groupId);

    @Query("SELECT d FROM Debt d WHERE (d.fromUser.id = :userId OR d.toUser.id = :userId) AND d.settled = false")
    List<Debt> findUnsettledDebtsByUser(@Param("userId") Long userId);

    @Query("SELECT d FROM Debt d WHERE (d.fromUser.id = :userId OR d.toUser.id = :userId) AND d.settled = false AND d.group.id = :groupId")
    List<Debt> findUnsettledDebtsByUserAndGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
