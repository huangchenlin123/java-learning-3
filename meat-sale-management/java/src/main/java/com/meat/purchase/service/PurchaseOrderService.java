package com.meat.purchase.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.purchase.entity.PurchaseOrder;
public interface PurchaseOrderService extends IService<PurchaseOrder> {
    void approve(Long orderId, boolean approved);
}
