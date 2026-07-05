package com.meat.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meat.finance.service.ReportService;
import com.meat.purchase.entity.GoodsCategory;
import com.meat.purchase.entity.GoodsSku;
import com.meat.purchase.entity.PurchaseOrder;
import com.meat.purchase.entity.Supplier;
import com.meat.purchase.mapper.GoodsCategoryMapper;
import com.meat.purchase.mapper.GoodsSkuMapper;
import com.meat.purchase.mapper.PurchaseOrderMapper;
import com.meat.purchase.mapper.SupplierMapper;
import com.meat.sale.entity.Customer;
import com.meat.sale.entity.SaleOrder;
import com.meat.sale.entity.SaleOrderItem;
import com.meat.sale.mapper.CustomerMapper;
import com.meat.sale.mapper.SaleOrderItemMapper;
import com.meat.sale.mapper.SaleOrderMapper;
import com.meat.warehouse.entity.BatchExpiryWarning;
import com.meat.warehouse.entity.Stock;
import com.meat.warehouse.entity.StockInItem;
import com.meat.warehouse.mapper.BatchExpiryWarningMapper;
import com.meat.warehouse.mapper.StockInItemMapper;
import com.meat.warehouse.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务实现 — 全部基于真实数据库查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final StockMapper stockMapper;
    private final StockInItemMapper stockInItemMapper;
    private final BatchExpiryWarningMapper expiryWarningMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsCategoryMapper goodsCategoryMapper;
    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;

    // ==================== Dashboard ====================

    @Override
    public Map<String, Object> dashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Map<String, Object> data = new HashMap<>();

        // 今日销售额
        BigDecimal todaySales = sumFinalAmount(todayStart, LocalDateTime.now());
        data.put("todaySales", todaySales);

        // 本月销售额
        BigDecimal monthSales = sumFinalAmount(monthStart, LocalDateTime.now());
        data.put("monthSales", monthSales);

        // 库存价值（Stock 数量 × 最近入库成本价）
        BigDecimal inventoryValue = calcInventoryValue();
        data.put("inventoryValue", inventoryValue);

        // 待处理采购单
        Long pendingOrders = purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getStatus, "PENDING"));
        data.put("pendingOrders", pendingOrders);

        // 即将到期预警数
        Long expiringSoon = expiryWarningMapper.selectCount(
                new LambdaQueryWrapper<BatchExpiryWarning>().eq(BatchExpiryWarning::getIsHandled, 0));
        data.put("expiringSoon", expiringSoon);

        // 毛利率（(销售额 - 成本) / 销售额）
        BigDecimal monthCost = calcMonthCost(monthStart);
        if (monthSales.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profit = monthSales.subtract(monthCost);
            data.put("grossProfit", profit.divide(monthSales, 4, RoundingMode.HALF_UP).doubleValue());
        } else {
            data.put("grossProfit", 0.0);
        }

        return data;
    }

    // ==================== 销售报表 ====================

    @Override
    public List<Map<String, Object>> salesReport(String period) {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, "COMPLETED")
                        .orderByDesc(SaleOrder::getCreateTime));

        DateTimeFormatter fmt;
        switch (period) {
            case "day":   fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd"); break;
            case "week":  fmt = DateTimeFormatter.ofPattern("yyyy-'W'ww"); break;
            default:      fmt = DateTimeFormatter.ofPattern("yyyy-MM");     break;
        }

        // 按期间分组聚合
        Map<String, List<SaleOrder>> grouped = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().format(fmt), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<SaleOrder>> entry : grouped.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", entry.getKey());
            row.put("amount", entry.getValue().stream()
                    .map(o -> o.getFinalAmount() != null ? o.getFinalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            row.put("orderCount", entry.getValue().size());
            result.add(row);
        }
        return result;
    }

    // ==================== 品类排行 ====================

    @Override
    public List<Map<String, Object>> categoryRanking() {
        // 加载全部 SKU → category 映射
        List<GoodsSku> skus = goodsSkuMapper.selectList(null);
        Map<Long, Long> skuCategoryMap = skus.stream()
                .collect(Collectors.toMap(GoodsSku::getId, s -> s.getCategoryId() != null ? s.getCategoryId() : 0L));
        List<GoodsCategory> categories = goodsCategoryMapper.selectList(null);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(GoodsCategory::getId, c -> c.getCategoryName() != null ? c.getCategoryName() : "未分类"));

        // 加载所有已完成订单的明细
        List<SaleOrder> completedOrders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getStatus, "COMPLETED"));
        List<Long> orderIds = completedOrders.stream().map(SaleOrder::getId).collect(Collectors.toList());
        if (orderIds.isEmpty()) return Collections.emptyList();

        List<SaleOrderItem> items = saleOrderItemMapper.selectList(
                new LambdaQueryWrapper<SaleOrderItem>().in(SaleOrderItem::getOrderId, orderIds));

        // 按品类汇总金额
        Map<Long, BigDecimal> categoryAmountMap = new LinkedHashMap<>();
        for (SaleOrderItem item : items) {
            Long catId = skuCategoryMap.getOrDefault(item.getSkuId(), 0L);
            BigDecimal amt = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            categoryAmountMap.merge(catId, amt, BigDecimal::add);
        }

        // 计算总额（effectively final）
        final BigDecimal totalAmount = categoryAmountMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按金额降序排列
        List<Map<String, Object>> result = new ArrayList<>();
        categoryAmountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("category", categoryNameMap.getOrDefault(e.getKey(), "未分类"));
                    row.put("amount", e.getValue());
                    row.put("ratio", totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? e.getValue().divide(totalAmount, 4, RoundingMode.HALF_UP).doubleValue()
                            : 0.0);
                    result.add(row);
                });
        return result;
    }

    // ==================== 供应商对账 ====================

    @Override
    public List<Map<String, Object>> supplierReconciliation() {
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .in(PurchaseOrder::getStatus, "APPROVED", "RECEIVED", "COMPLETED"));

        Map<Long, String> supplierNameMap = supplierMapper.selectList(null).stream()
                .collect(Collectors.toMap(Supplier::getId, s -> s.getSupplierName() != null ? s.getSupplierName() : "未知"));

        Map<Long, List<PurchaseOrder>> grouped = orders.stream()
                .filter(o -> o.getSupplierId() != null)
                .collect(Collectors.groupingBy(PurchaseOrder::getSupplierId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<PurchaseOrder>> entry : grouped.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("supplierId", entry.getKey());
            row.put("supplierName", supplierNameMap.getOrDefault(entry.getKey(), "未知"));
            row.put("totalAmount", entry.getValue().stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            row.put("orderCount", entry.getValue().size());
            result.add(row);
        }
        result.sort((a, b) -> ((BigDecimal) b.get("totalAmount")).compareTo((BigDecimal) a.get("totalAmount")));
        return result;
    }

    // ==================== 客户对账 ====================

    @Override
    public List<Map<String, Object>> customerReconciliation() {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, "COMPLETED")
                        .isNotNull(SaleOrder::getCustomerId));

        Map<Long, String> customerNameMap = customerMapper.selectList(null).stream()
                .collect(Collectors.toMap(Customer::getId, c -> c.getCustomerName() != null ? c.getCustomerName() : "未知"));

        Map<Long, List<SaleOrder>> grouped = orders.stream()
                .filter(o -> o.getCustomerId() != null)
                .collect(Collectors.groupingBy(SaleOrder::getCustomerId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<SaleOrder>> entry : grouped.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("customerId", entry.getKey());
            row.put("customerName", customerNameMap.getOrDefault(entry.getKey(), "未知"));
            row.put("totalAmount", entry.getValue().stream()
                    .map(o -> o.getFinalAmount() != null ? o.getFinalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            row.put("orderCount", entry.getValue().size());
            result.add(row);
        }
        result.sort((a, b) -> ((BigDecimal) b.get("totalAmount")).compareTo((BigDecimal) a.get("totalAmount")));
        return result;
    }

    // ==================== 库存价值报表 ====================

    @Override
    public List<Map<String, Object>> inventoryReport() {
        List<Stock> stocks = stockMapper.selectList(
                new LambdaQueryWrapper<Stock>().gt(Stock::getQuantity, BigDecimal.ZERO));
        if (stocks.isEmpty()) return Collections.emptyList();

        // SKU → 品类 映射
        Map<Long, Long> skuCategoryMap = goodsSkuMapper.selectList(null).stream()
                .collect(Collectors.toMap(GoodsSku::getId, s -> s.getCategoryId() != null ? s.getCategoryId() : 0L));
        Map<Long, String> catNameMap = goodsCategoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(GoodsCategory::getId, c -> c.getCategoryName() != null ? c.getCategoryName() : "未分类"));
        Map<Long, String> skuNameMap = goodsSkuMapper.selectList(null).stream()
                .collect(Collectors.toMap(GoodsSku::getId, s -> s.getSkuName() != null ? s.getSkuName() : "未知"));

        // 尝试获取最近入库成本
        Map<Long, BigDecimal> skuCostMap = new HashMap<>();
        List<StockInItem> allInItems = stockInItemMapper.selectList(null);
        for (StockInItem item : allInItems) {
            if (item.getCostPrice() != null) {
                skuCostMap.putIfAbsent(item.getSkuId(), item.getCostPrice());
            }
        }

        // 按品类汇总
        Map<Long, BigDecimal> catQtyMap = new HashMap<>();
        Map<Long, BigDecimal> catValueMap = new HashMap<>();
        for (Stock stock : stocks) {
            Long catId = skuCategoryMap.getOrDefault(stock.getSkuId(), 0L);
            BigDecimal qty = stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO;
            BigDecimal cost = skuCostMap.getOrDefault(stock.getSkuId(), BigDecimal.ZERO);
            catQtyMap.merge(catId, qty, BigDecimal::add);
            catValueMap.merge(catId, qty.multiply(cost), BigDecimal::add);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long catId : catQtyMap.keySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("category", catNameMap.getOrDefault(catId, "未分类"));
            row.put("quantity", catQtyMap.get(catId));
            row.put("value", catValueMap.getOrDefault(catId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            result.add(row);
        }
        result.sort((a, b) -> ((BigDecimal) b.get("value")).compareTo((BigDecimal) a.get("value")));
        return result;
    }

    // ==================== 毛利报表 ====================

    @Override
    public List<Map<String, Object>> profitReport(String period) {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, "COMPLETED")
                        .orderByAsc(SaleOrder::getCreateTime));

        DateTimeFormatter fmt;
        switch (period) {
            case "day":  fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd"); break;
            case "week": fmt = DateTimeFormatter.ofPattern("yyyy-'W'ww"); break;
            default:     fmt = DateTimeFormatter.ofPattern("yyyy-MM");     break;
        }

        // 按期间分组
        Map<String, List<SaleOrder>> grouped = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().format(fmt), LinkedHashMap::new, Collectors.toList()));

        // 获取 SKU 成本
        Map<Long, BigDecimal> skuCostMap = new HashMap<>();
        for (StockInItem item : stockInItemMapper.selectList(null)) {
            if (item.getCostPrice() != null) {
                skuCostMap.putIfAbsent(item.getSkuId(), item.getCostPrice());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<SaleOrder>> entry : grouped.entrySet()) {
            BigDecimal sales = entry.getValue().stream()
                    .map(o -> o.getFinalAmount() != null ? o.getFinalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 获取期间内所有订单明细的成本
            List<Long> orderIds = entry.getValue().stream().map(SaleOrder::getId).collect(Collectors.toList());
            List<SaleOrderItem> items = saleOrderItemMapper.selectList(
                    new LambdaQueryWrapper<SaleOrderItem>().in(SaleOrderItem::getOrderId, orderIds));

            BigDecimal cost = BigDecimal.ZERO;
            for (SaleOrderItem item : items) {
                BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                BigDecimal unitCost = skuCostMap.getOrDefault(item.getSkuId(), BigDecimal.ZERO);
                cost = cost.add(qty.multiply(unitCost));
            }

            BigDecimal profit = sales.subtract(cost);
            Map<String, Object> row = new HashMap<>();
            row.put("date", entry.getKey());
            row.put("sales", sales);
            row.put("cost", cost.setScale(2, RoundingMode.HALF_UP));
            row.put("profit", profit.setScale(2, RoundingMode.HALF_UP));
            row.put("marginRate", sales.compareTo(BigDecimal.ZERO) > 0
                    ? profit.divide(sales, 4, RoundingMode.HALF_UP).doubleValue() : 0.0);
            result.add(row);
        }
        return result;
    }

    // ==================== 辅助方法 ====================

    private BigDecimal sumFinalAmount(LocalDateTime from, LocalDateTime to) {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, "COMPLETED")
                        .between(SaleOrder::getCreateTime, from, to));
        return orders.stream()
                .map(o -> o.getFinalAmount() != null ? o.getFinalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcInventoryValue() {
        List<Stock> stocks = stockMapper.selectList(
                new LambdaQueryWrapper<Stock>().gt(Stock::getQuantity, BigDecimal.ZERO));
        if (stocks.isEmpty()) return BigDecimal.ZERO;

        // 取每个 SKU 的最近入库成本
        Map<Long, BigDecimal> skuCostMap = new HashMap<>();
        for (StockInItem item : stockInItemMapper.selectList(
                new LambdaQueryWrapper<StockInItem>().orderByDesc(StockInItem::getCreateTime))) {
            if (item.getCostPrice() != null) {
                skuCostMap.putIfAbsent(item.getSkuId(), item.getCostPrice());
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Stock stock : stocks) {
            BigDecimal qty = stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO;
            BigDecimal cost = skuCostMap.getOrDefault(stock.getSkuId(), BigDecimal.ZERO);
            total = total.add(qty.multiply(cost));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcMonthCost(LocalDateTime monthStart) {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, "COMPLETED")
                        .ge(SaleOrder::getCreateTime, monthStart));
        List<Long> orderIds = orders.stream().map(SaleOrder::getId).collect(Collectors.toList());
        if (orderIds.isEmpty()) return BigDecimal.ZERO;

        List<SaleOrderItem> items = saleOrderItemMapper.selectList(
                new LambdaQueryWrapper<SaleOrderItem>().in(SaleOrderItem::getOrderId, orderIds));

        Map<Long, BigDecimal> skuCostMap = new HashMap<>();
        for (StockInItem inItem : stockInItemMapper.selectList(
                new LambdaQueryWrapper<StockInItem>().orderByDesc(StockInItem::getCreateTime))) {
            if (inItem.getCostPrice() != null) skuCostMap.putIfAbsent(inItem.getSkuId(), inItem.getCostPrice());
        }

        BigDecimal cost = BigDecimal.ZERO;
        for (SaleOrderItem item : items) {
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal unitCost = skuCostMap.getOrDefault(item.getSkuId(), BigDecimal.ZERO);
            cost = cost.add(qty.multiply(unitCost));
        }
        return cost;
    }
}
