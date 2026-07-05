package com.meat.purchase.service.impl;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.purchase.entity.*;
import com.meat.purchase.mapper.*;
import com.meat.purchase.service.PurchaseOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    @Resource private PurchaseOrderItemMapper itemMapper;

    @Override
    @Transactional
    public boolean save(PurchaseOrder order) {
        if (order.getSupplierId() == null) throw new BusinessException("请选择供应商");
        order.setStatus("PENDING");
        order.setCreateBy(StpUtil.getLoginIdAsLong());
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("PO" + java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + String.format("%06d", System.nanoTime() % 1000000));
        }
        if (order.getTotalAmount() == null) order.setTotalAmount(BigDecimal.ZERO);
        super.save(order);
        if (order.getItems() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (PurchaseOrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                if (item.getAmount() == null) {
                    item.setAmount(item.getPrice().multiply(item.getQuantity()));
                }
                total = total.add(item.getAmount());
                itemMapper.insert(item);
            }
            order.setTotalAmount(total);
            super.updateById(order);
        }
        return true;
    }

    @Override
    @Transactional
    public void approve(Long orderId, boolean approved) {
        PurchaseOrder order = getById(orderId);
        if (order == null) throw new BusinessException("采购单不存在");
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException("当前状态不可审批");
        order.setStatus(approved ? "APPROVED" : "CANCELLED");
        order.setApproveBy(StpUtil.getLoginIdAsLong());
        order.setApproveTime(LocalDateTime.now());
        updateById(order);
    }
}
