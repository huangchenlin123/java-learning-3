package com.meat.warehouse.scheduler;

import com.meat.warehouse.service.ExpiryWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 效期预警定时任务 — 每天早 8:00 扫描库存批次效期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryWarningScheduler {

    private final ExpiryWarningService expiryWarningService;

    /**
     * 每天 8:00 执行全库效期扫描
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scanExpiry() {
        log.info("===== 效期预警定时扫描开始 =====");
        try {
            int count = expiryWarningService.scanAndWarn();
            log.info("===== 效期预警扫描结束: {} 条预警 =====", count);
        } catch (Exception e) {
            log.error("效期预警扫描异常: {}", e.getMessage(), e);
        }
    }
}
