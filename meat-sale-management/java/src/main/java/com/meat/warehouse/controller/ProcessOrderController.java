package com.meat.warehouse.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.warehouse.entity.ProcessBom;
import com.meat.warehouse.entity.ProcessBomItem;
import com.meat.warehouse.entity.ProcessItemOut;
import com.meat.warehouse.entity.ProcessOrder;
import com.meat.warehouse.mapper.ProcessBomItemMapper;
import com.meat.warehouse.mapper.ProcessBomMapper;
import com.meat.warehouse.mapper.ProcessItemOutMapper;
import com.meat.warehouse.service.ProcessOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分割加工管理控制器
 */
@Api(tags = "分割加工管理")
@RestController
@RequestMapping("/api/warehouse/process")
@RequiredArgsConstructor
public class ProcessOrderController {

    private final ProcessOrderService processOrderService;
    private final ProcessBomMapper bomMapper;
    private final ProcessBomItemMapper bomItemMapper;
    private final ProcessItemOutMapper processItemOutMapper;

    // ==================== 加工单 CRUD ====================

    @GetMapping
    @ApiOperation("加工单分页查询")
    public R<Page<ProcessOrder>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<ProcessOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProcessOrder> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            w.eq(ProcessOrder::getStatus, status);
        }
        w.orderByDesc(ProcessOrder::getCreateTime);
        return R.ok(processOrderService.page(page, w));
    }

    @GetMapping("/{id}")
    @ApiOperation("加工单详情（含产出明细）")
    public R<ProcessOrder> getById(@PathVariable Long id) {
        ProcessOrder order = processOrderService.getById(id);
        if (order != null) {
            List<ProcessItemOut> outputs = processItemOutMapper.selectList(
                    new LambdaQueryWrapper<ProcessItemOut>().eq(ProcessItemOut::getProcessId, id));
            order.setOutputs(outputs);
        }
        return R.ok(order);
    }

    @PostMapping
    @ApiOperation("创建加工单")
    public R<ProcessOrder> save(@RequestBody ProcessOrder order) {
        Long userId = StpUtil.getLoginIdAsLong();
        order.setCreateBy(userId);
        order.setStatus("PENDING");
        if (order.getProcessNo() == null) {
            order.setProcessNo("PR" + java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + String.format("%06d", System.nanoTime() % 1000000));
        }
        // 必填字段默认值
        if (order.getWarehouseId() == null) order.setWarehouseId(1L);
        if (order.getRawBatchId() == null) order.setRawBatchId(0L);
        if (order.getRawQuantity() == null) order.setRawQuantity(java.math.BigDecimal.ZERO);
        processOrderService.save(order);

        // 级联保存产出明细（含预计产出量）
        if (order.getOutputs() != null) {
            for (ProcessItemOut out : order.getOutputs()) {
                out.setProcessId(order.getId());
                processItemOutMapper.insert(out);
            }
        }
        return R.ok(order);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新加工单及产出明细")
    public R<Void> update(@PathVariable Long id, @RequestBody ProcessOrder order) {
        order.setId(id);
        processOrderService.updateById(order);
        // 先删旧产出再插入
        processItemOutMapper.delete(
                new LambdaQueryWrapper<ProcessItemOut>().eq(ProcessItemOut::getProcessId, id));
        if (order.getOutputs() != null) {
            for (ProcessItemOut out : order.getOutputs()) {
                out.setProcessId(id);
                processItemOutMapper.insert(out);
            }
        }
        return R.ok();
    }

    @PutMapping("/{id}/complete")
    @ApiOperation("确认完成加工（原料出库 → 成本分摊 → 产出入库）")
    public R<Void> complete(@PathVariable Long id, @RequestBody(required = false) ProcessOrder body) {
        // 如果请求体含产出明细，先更新
        if (body != null && body.getOutputs() != null && !body.getOutputs().isEmpty()) {
            processItemOutMapper.delete(
                    new LambdaQueryWrapper<ProcessItemOut>().eq(ProcessItemOut::getProcessId, id));
            for (ProcessItemOut out : body.getOutputs()) {
                out.setProcessId(id);
                processItemOutMapper.insert(out);
            }
        }
        Long userId = StpUtil.getLoginIdAsLong();
        processOrderService.completeProcess(id, userId);
        return R.ok();
    }

    // ==================== BOM 模板管理 ====================

    @GetMapping("/bom")
    @ApiOperation("BOM 模板列表（含明细）")
    public R<List<ProcessBom>> bomList() {
        List<ProcessBom> boms = bomMapper.selectList(null);
        for (ProcessBom bom : boms) {
            List<ProcessBomItem> items = bomItemMapper.selectList(
                    new LambdaQueryWrapper<ProcessBomItem>().eq(ProcessBomItem::getBomId, bom.getId()));
            bom.setItems(items);
        }
        return R.ok(boms);
    }

    @GetMapping("/bom/{id}")
    @ApiOperation("BOM 模板详情（含明细）")
    public R<ProcessBom> bomDetail(@PathVariable Long id) {
        ProcessBom bom = bomMapper.selectById(id);
        if (bom != null) {
            List<ProcessBomItem> items = bomItemMapper.selectList(
                    new LambdaQueryWrapper<ProcessBomItem>()
                            .eq(ProcessBomItem::getBomId, id)
                            .orderByAsc(ProcessBomItem::getSort));
            bom.setItems(items);
        }
        return R.ok(bom);
    }

    @PostMapping("/bom")
    @ApiOperation("创建 BOM 模板")
    public R<ProcessBom> saveBom(@RequestBody ProcessBom bom) {
        bomMapper.insert(bom);
        if (bom.getItems() != null) {
            for (ProcessBomItem item : bom.getItems()) {
                item.setBomId(bom.getId());
                bomItemMapper.insert(item);
            }
        }
        return R.ok(bom);
    }

    @PutMapping("/bom/{id}")
    @ApiOperation("更新 BOM 模板")
    public R<Void> updateBom(@PathVariable Long id, @RequestBody ProcessBom bom) {
        bom.setId(id);
        bomMapper.updateById(bom);
        bomItemMapper.delete(new LambdaQueryWrapper<ProcessBomItem>().eq(ProcessBomItem::getBomId, id));
        if (bom.getItems() != null) {
            for (ProcessBomItem item : bom.getItems()) {
                item.setBomId(id);
                bomItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    @DeleteMapping("/bom/{id}")
    @ApiOperation("删除 BOM 模板")
    public R<Void> deleteBom(@PathVariable Long id) {
        bomItemMapper.delete(new LambdaQueryWrapper<ProcessBomItem>().eq(ProcessBomItem::getBomId, id));
        bomMapper.deleteById(id);
        return R.ok();
    }

    // ==================== 产出明细查询 ====================

    @GetMapping("/{processId}/outputs")
    @ApiOperation("查询加工单产出明细")
    public R<List<ProcessItemOut>> listOutputs(@PathVariable Long processId) {
        return R.ok(processItemOutMapper.selectList(
                new LambdaQueryWrapper<ProcessItemOut>().eq(ProcessItemOut::getProcessId, processId)));
    }
}
