package com.meat.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.warehouse.entity.ProcessOrder;

/**
 * 分割加工服务
 */
public interface ProcessOrderService extends IService<ProcessOrder> {

    /**
     * 执行加工完成流程：
     * 1. 校验订单和 BOM
     * 2. 原料 FIFO 出库
     * 3. 按实际产出重量比例分摊成本
     * 4. 产出品入库 + 生成批次
     * 5. 更新订单状态
     *
     * @param processId 加工单 ID
     * @param userId    操作人 ID
     */
    void completeProcess(Long processId, Long userId);
}
