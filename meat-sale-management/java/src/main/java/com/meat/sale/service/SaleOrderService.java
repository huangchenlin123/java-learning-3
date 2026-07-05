package com.meat.sale.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.sale.entity.SaleOrder;
public interface SaleOrderService extends IService<SaleOrder> {
    void posSale(SaleOrder order);
    void approveOrder(Long orderId, boolean approved);
}
