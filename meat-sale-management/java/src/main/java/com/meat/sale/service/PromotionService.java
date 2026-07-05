package com.meat.sale.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.SaleOrder;
import java.math.BigDecimal;
import java.util.List;
public interface PromotionService extends IService<Promotion> {
    BigDecimal calculateDiscount(SaleOrder order);
}
