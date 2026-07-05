package com.meat.warehouse.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.warehouse.entity.StockIn;
public interface StockInService extends IService<StockIn> {
    void confirm(Long stockInId, Long userId);
}
