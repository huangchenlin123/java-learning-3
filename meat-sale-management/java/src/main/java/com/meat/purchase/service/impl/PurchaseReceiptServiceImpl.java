package com.meat.purchase.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.purchase.entity.*;
import com.meat.purchase.mapper.*;
import com.meat.purchase.service.PurchaseReceiptService;
import com.meat.warehouse.entity.*;
import com.meat.warehouse.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseReceiptServiceImpl extends ServiceImpl<PurchaseReceiptMapper, PurchaseReceipt> implements PurchaseReceiptService {

    @Resource private PurchaseReceiptItemMapper receiptItemMapper;
    @Resource private PurchaseOrderMapper orderMapper;
    @Resource private PurchaseOrderItemMapper orderItemMapper;
    @Resource private StockInMapper stockInMapper;
    @Resource private StockInItemMapper stockInItemMapper;
    @Resource private BatchMapper batchMapper;
    @Resource private StockMapper stockMapper;

    @Override
    @Transactional
    public boolean save(PurchaseReceipt receipt) {
        PurchaseOrder order = orderMapper.selectById(receipt.getOrderId());
        if (order == null) throw new BusinessException("采购单不存在");
        receipt.setStatus("PENDING");
        if (receipt.getReceiptNo() == null || receipt.getReceiptNo().isEmpty()) {
            receipt.setReceiptNo("RC" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + String.format("%06d", System.nanoTime() % 1000000));
        }
        super.save(receipt);

        // 若未提供明细，则从采购单自动加载
        List<PurchaseReceiptItem> items = receipt.getItems();
        if (items == null || items.isEmpty()) {
            List<PurchaseOrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, receipt.getOrderId()));
            items = new ArrayList<>();
            for (PurchaseOrderItem oi : orderItems) {
                PurchaseReceiptItem ri = new PurchaseReceiptItem();
                ri.setReceiptId(receipt.getId());
                ri.setOrderItemId(oi.getId());
                ri.setSkuId(oi.getSkuId());
                ri.setOrderQuantity(oi.getQuantity());
                ri.setPrice(oi.getPrice());
                ri.setActualQuantity(oi.getQuantity());
                items.add(ri);
            }
        }

        // 保存明细并计算实际总重量
        if (items != null && !items.isEmpty()) {
            BigDecimal totalWeight = BigDecimal.ZERO;
            for (PurchaseReceiptItem item : items) {
                item.setReceiptId(receipt.getId());
                receiptItemMapper.insert(item);
                if (item.getActualQuantity() != null) totalWeight = totalWeight.add(item.getActualQuantity());
            }
            receipt.setActualWeight(totalWeight);
            super.updateById(receipt);
        }
        return true;
    }

    @Override
    @Transactional
    public void confirmReceipt(PurchaseReceipt receipt) {
        PurchaseReceipt db = getById(receipt.getId());
        if (db == null) throw new BusinessException("验收单不存在");
        PurchaseOrder order = orderMapper.selectById(db.getOrderId());

        StockIn stockIn = new StockIn();
        stockIn.setInNo("IN" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%06d", System.nanoTime() % 1000000));
        stockIn.setInType("PURCHASE");
        stockIn.setSourceId(order.getId());
        stockIn.setWarehouseId(1L);
        stockIn.setTotalQuantity(db.getActualWeight());
        stockIn.setStatus("CONFIRMED");
        stockIn.setConfirmTime(java.time.LocalDateTime.now());
        stockInMapper.insert(stockIn);

        List<PurchaseReceiptItem> receiptItems = receiptItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseReceiptItem>().eq(PurchaseReceiptItem::getReceiptId, db.getId()));

        // 兜底：若无明细但总重量>0，则创建默认明细
        if ((receiptItems == null || receiptItems.isEmpty())
                && db.getActualWeight() != null && db.getActualWeight().compareTo(BigDecimal.ZERO) > 0) {
            Long skuId = 1L; // 默认SKU
            BigDecimal price = BigDecimal.ZERO;

            // 尝试从采购单加载
            List<PurchaseOrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, db.getOrderId()));
            if (orderItems != null && !orderItems.isEmpty()) {
                skuId = orderItems.get(0).getSkuId();
                price = orderItems.get(0).getPrice() != null ? orderItems.get(0).getPrice() : BigDecimal.ZERO;
            } else {
                // 尝试从采购单总金额估算单价
                if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                    price = order.getTotalAmount().divide(db.getActualWeight(), 2, RoundingMode.HALF_UP);
                }
            }

            PurchaseReceiptItem ri = new PurchaseReceiptItem();
            ri.setReceiptId(db.getId());
            ri.setOrderItemId(0L);
            ri.setSkuId(skuId);
            ri.setPrice(price);
            ri.setOrderQuantity(db.getActualWeight());
            ri.setActualQuantity(db.getActualWeight());
            receiptItemMapper.insert(ri);
            receiptItems = java.util.Collections.singletonList(ri);
        }

        for (PurchaseReceiptItem item : receiptItems) {
            Batch batch = new Batch();
            batch.setBatchNo("BT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + String.format("%06d", System.nanoTime() % 1000000));
            batch.setSkuId(item.getSkuId());
            batch.setProductDate(db.getSlaughterDate());
            batch.setShelfLife(30);
            if (db.getSlaughterDate() != null) batch.setExpireDate(db.getSlaughterDate().plusDays(30));
            batch.setSourceType("PURCHASE");
            batch.setSourceId(db.getId());
            batch.setQuarantineNo(db.getQuarantineNo());
            batch.setOrigin(db.getOrigin());
            batchMapper.insert(batch);

            StockInItem inItem = new StockInItem();
            inItem.setInId(stockIn.getId());
            inItem.setSkuId(item.getSkuId());
            inItem.setBatchId(batch.getId());
            inItem.setQuantity(item.getActualQuantity());
            inItem.setCostPrice(item.getPrice());
            stockInItemMapper.insert(inItem);

            // 直接更新库存
            Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                    .eq(Stock::getSkuId, item.getSkuId())
                    .eq(Stock::getBatchId, batch.getId())
                    .eq(Stock::getWarehouseId, 1L));
            if (stock != null) {
                stock.setQuantity(stock.getQuantity().add(item.getActualQuantity()));
                stockMapper.updateById(stock);
            } else {
                stock = new Stock();
                stock.setSkuId(item.getSkuId());
                stock.setBatchId(batch.getId());
                stock.setWarehouseId(1L);
                stock.setQuantity(item.getActualQuantity());
                stock.setLockedQuantity(BigDecimal.ZERO);
                stockMapper.insert(stock);
            }
        }

        db.setStatus("CONFIRMED");
        updateById(db);
        order.setStatus("RECEIVED");
        orderMapper.updateById(order);
    }
}
