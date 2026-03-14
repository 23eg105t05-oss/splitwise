package com.splitwise.middleware;

import com.splitwise.dto.response.SimplifiedDebtResponse;
import com.splitwise.model.Group;
import com.splitwise.repository.GroupRepository;
import com.splitwise.service.DebtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job that runs every day at midnight.
 * Logs simplified debt summary for all active groups.
 * Can be extended to trigger push notifications or persist results.
 */
@Component
public class DebtSimplificationJob {

    private static final Logger logger = LoggerFactory.getLogger(DebtSimplificationJob.class);

    private final GroupRepository groupRepository;
    private final DebtService debtService;

    public DebtSimplificationJob(GroupRepository groupRepository, DebtService debtService) {
        this.groupRepository = groupRepository;
        this.debtService = debtService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyDebtSimplification() {
        logger.info("[Job] Running daily debt simplification job...");
        try {
            List<Group> activeGroups = groupRepository.findByActiveTrue();
            for (Group group : activeGroups) {
                List<SimplifiedDebtResponse> simplified = debtService.getSimplifiedDebts(group.getId());
                logger.info("[Job] Group '{}' (id={}): {} simplified transactions.",
                        group.getName(), group.getId(), simplified.size());
            }
            logger.info("[Job] Debt simplification job completed successfully.");
        } catch (Exception e) {
            logger.error("[Job] Debt simplification job failed: {}", e.getMessage(), e);
        }
    }
}
