package com.meat.promotion.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.PromotionRule;
import com.meat.sale.entity.SaleOrder;
import com.meat.sale.mapper.PromotionRuleMapper;
import com.meat.sale.service.PromotionService;
import com.meat.sale.service.impl.PromotionServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 促销管理控制器
 */
@Api(tags = "促销管理")
@RestController
@RequestMapping("/api/promotion")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionRuleMapper promotionRuleMapper;

    // ==================== 促销活动 CRUD ====================

    @GetMapping @ApiOperation("分页查询促销活动")
    public R<Page<Promotion>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String promoType,
            @RequestParam(required = false) Integer status) {
        Page<Promotion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Promotion> w = new LambdaQueryWrapper<>();
        if (promoType != null && !promoType.isEmpty()) {
            w.eq(Promotion::getPromoType, promoType);
        }
        if (status != null) {
            w.eq(Promotion::getStatus, status);
        }
        w.orderByDesc(Promotion::getPriority).orderByDesc(Promotion::getCreateTime);
        return R.ok(promotionService.page(page, w));
    }

    @GetMapping("/{id}") @ApiOperation("促销活动详情")
    public R<Promotion> getById(@PathVariable Long id) {
        Promotion promo = promotionService.getById(id);
        if (promo != null) {
            // 加载关联规则
            List<PromotionRule> rules = promotionRuleMapper.selectList(
                    new LambdaQueryWrapper<PromotionRule>().eq(PromotionRule::getPromoId, id));
            promo.setRules(rules);
        }
        return R.ok(promo);
    }

    @PostMapping @ApiOperation("创建促销活动")
    public R<Void> save(@RequestBody Promotion promotion) {
        promotionService.save(promotion);
        // 级联保存规则
        if (promotion.getRules() != null) {
            for (PromotionRule rule : promotion.getRules()) {
                rule.setPromoId(promotion.getId());
                promotionRuleMapper.insert(rule);
            }
        }
        return R.ok();
    }

    @PutMapping("/{id}") @ApiOperation("更新促销活动")
    public R<Void> update(@PathVariable Long id, @RequestBody Promotion promotion) {
        promotion.setId(id);
        promotionService.updateById(promotion);
        // 先删旧规则再插入新规则
        promotionRuleMapper.delete(new LambdaQueryWrapper<PromotionRule>().eq(PromotionRule::getPromoId, id));
        if (promotion.getRules() != null) {
            for (PromotionRule rule : promotion.getRules()) {
                rule.setPromoId(id);
                promotionRuleMapper.insert(rule);
            }
        }
        return R.ok();
    }

    @DeleteMapping("/{id}") @ApiOperation("删除促销活动")
    public R<Void> delete(@PathVariable Long id) {
        promotionRuleMapper.delete(new LambdaQueryWrapper<PromotionRule>().eq(PromotionRule::getPromoId, id));
        promotionService.removeById(id);
        return R.ok();
    }

    @PutMapping("/{id}/status") @ApiOperation("启用/停用促销活动")
    public R<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        Promotion promo = new Promotion();
        promo.setId(id);
        promo.setStatus(status);
        promotionService.updateById(promo);
        return R.ok();
    }

    // ==================== 促销规则 CRUD ====================

    @GetMapping("/{promoId}/rules") @ApiOperation("查询活动的规则列表")
    public R<List<PromotionRule>> listRules(@PathVariable Long promoId) {
        return R.ok(promotionRuleMapper.selectList(
                new LambdaQueryWrapper<PromotionRule>().eq(PromotionRule::getPromoId, promoId)));
    }

    @PostMapping("/{promoId}/rules") @ApiOperation("为活动添加规则")
    public R<Void> addRule(@PathVariable Long promoId, @RequestBody PromotionRule rule) {
        rule.setPromoId(promoId);
        promotionRuleMapper.insert(rule);
        return R.ok();
    }

    @PutMapping("/rules/{ruleId}") @ApiOperation("更新规则")
    public R<Void> updateRule(@PathVariable Long ruleId, @RequestBody PromotionRule rule) {
        rule.setId(ruleId);
        promotionRuleMapper.updateById(rule);
        return R.ok();
    }

    @DeleteMapping("/rules/{ruleId}") @ApiOperation("删除规则")
    public R<Void> deleteRule(@PathVariable Long ruleId) {
        promotionRuleMapper.deleteById(ruleId);
        return R.ok();
    }

    // ==================== 试算接口 ====================

    @PostMapping("/preview") @ApiOperation("促销试算 — 计算订单可享受的优惠")
    public R<?> preview(@RequestBody SaleOrder order) {
        if (promotionService instanceof PromotionServiceImpl) {
            return R.ok(((PromotionServiceImpl) promotionService).calculateFull(order));
        }
        return R.ok(promotionService.calculateDiscount(order));
    }
}
