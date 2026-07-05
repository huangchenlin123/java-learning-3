package com.meat.finance.service;

import java.util.List;
import java.util.Map;

/**
 * 报表服务 — 经营概览 + 销售/品类/对账/库存/利润报表
 */
public interface ReportService {

    /** 经营概览 Dashboard */
    Map<String, Object> dashboard();

    /** 销售报表（按 period: day/week/month） */
    List<Map<String, Object>> salesReport(String period);

    /** 品类销售排行 */
    List<Map<String, Object>> categoryRanking();

    /** 供应商对账（采购汇总） */
    List<Map<String, Object>> supplierReconciliation();

    /** 客户对账（销售汇总） */
    List<Map<String, Object>> customerReconciliation();

    /** 库存价值报表 */
    List<Map<String, Object>> inventoryReport();

    /** 毛利报表 */
    List<Map<String, Object>> profitReport(String period);
}
