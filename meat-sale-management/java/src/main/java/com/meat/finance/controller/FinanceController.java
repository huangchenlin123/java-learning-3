package com.meat.finance.controller;

import com.meat.common.R;
import com.meat.finance.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 财务管理 — 报表接口（全部基于真实数据库查询）
 */
@Api(tags = "财务管理")
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @ApiOperation("经营概览 — 今日/本月销售额、库存价值、待处理订单、到期预警、毛利率")
    public R<Map<String, Object>> dashboard() {
        return R.ok(reportService.dashboard());
    }

    @GetMapping("/sales/report")
    @ApiOperation("销售报表 — 按日/周/月聚合销售额和订单数")
    public R<List<Map<String, Object>>> salesReport(
            @RequestParam(defaultValue = "month") String period) {
        return R.ok(reportService.salesReport(period));
    }

    @GetMapping("/category/ranking")
    @ApiOperation("品类销售排行 — 按品类汇总销售额及占比")
    public R<List<Map<String, Object>>> categoryRanking() {
        return R.ok(reportService.categoryRanking());
    }

    @GetMapping("/supplier/reconciliation")
    @ApiOperation("供应商对账 — 按供应商汇总采购金额和订单数")
    public R<List<Map<String, Object>>> supplierReconciliation() {
        return R.ok(reportService.supplierReconciliation());
    }

    @GetMapping("/customer/reconciliation")
    @ApiOperation("客户对账 — 按客户汇总消费金额和订单数")
    public R<List<Map<String, Object>>> customerReconciliation() {
        return R.ok(reportService.customerReconciliation());
    }

    @GetMapping("/inventory/report")
    @ApiOperation("库存价值报表 — 按品类汇总库存数量和估算价值")
    public R<List<Map<String, Object>>> inventoryReport() {
        return R.ok(reportService.inventoryReport());
    }

    @GetMapping("/profit/report")
    @ApiOperation("毛利报表 — 按期间计算销售额、成本、毛利和毛利率")
    public R<List<Map<String, Object>>> profitReport(
            @RequestParam(defaultValue = "month") String period) {
        return R.ok(reportService.profitReport(period));
    }
}
