package com.meat.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.warehouse.entity.BatchExpiryWarning;

import java.util.List;

/**
 * 效期预警服务
 */
public interface ExpiryWarningService extends IService<BatchExpiryWarning> {

    /**
     * 执行全库效期扫描并生成预警记录
     *
     * @return 本次生成的预警记录数
     */
    int scanAndWarn();

    /**
     * 查询所有未处理的预警
     *
     * @param warnLevel 预警级别（可选）
     */
    List<BatchExpiryWarning> listUnhandled(String warnLevel);

    /**
     * 标记预警为已处理
     */
    void markHandled(Long warningId, Long userId);
}
