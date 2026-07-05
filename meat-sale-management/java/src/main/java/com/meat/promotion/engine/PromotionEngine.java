package com.meat.promotion.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meat.promotion.strategy.BuyGiftStrategy;
import com.meat.promotion.strategy.PromotionContext;
import com.meat.promotion.strategy.PromotionStrategy;
import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.PromotionRule;
import com.meat.sale.mapper.PromotionMapper;
import com.meat.sale.mapper.PromotionRuleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 促销引擎 — 加载有效促销活动并链式执行策略计算
 * <p>
 * 计算流程：
 * <ol>
 *   <li>从数据库加载当前时间有效的促销活动（status=1 且在有效期内）</li>
 *   <li>加载每个活动的规则列表</li>
 *   <li>按 priority 降序排列（优先级高的先执行）</li>
 *   <li>依次匹配对应策略并计算优惠</li>
 *   <li>优惠金额链式叠加（后续策略基于扣减后的有效金额计算）</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
public class PromotionEngine {

    @Resource
    private PromotionMapper promotionMapper;

    @Resource
    private PromotionRuleMapper promotionRuleMapper;

    /**
     * 所有策略实现（Spring 自动注入）
     */
    @Resource
    private List<PromotionStrategy> strategies;

    /**
     * 计算订单可享受的全部促销优惠
     *
     * @param ctx 促销上下文（含订单和明细）
     * @return 促销计算结果（优惠金额 + 赠品列表 + 命中活动）
     */
    public PromotionResult calculate(PromotionContext ctx) {
        PromotionResult result = PromotionResult.builder().build();

        // 1. 加载有效促销活动
        List<Promotion> activePromotions = loadActivePromotions();
        if (activePromotions.isEmpty()) {
            log.debug("无有效促销活动");
            return result;
        }

        // 2. 按优先级降序排列
        activePromotions.sort(Comparator.comparingInt(Promotion::getPriority).reversed());

        // 3. 构建 type -> strategy 映射
        Map<String, PromotionStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(PromotionStrategy::getType, s -> s, (a, b) -> a));

        // 4. 加载规则并加载到 promotion 对象
        for (Promotion promo : activePromotions) {
            List<PromotionRule> rules = promotionRuleMapper.selectList(
                    new LambdaQueryWrapper<PromotionRule>().eq(PromotionRule::getPromoId, promo.getId()));
            promo.setRules(rules);
        }

        // 5. 链式执行策略
        BigDecimal accumulatedDiscount = BigDecimal.ZERO;
        for (Promotion promo : activePromotions) {
            PromotionStrategy strategy = strategyMap.get(promo.getPromoType());
            if (strategy == null) {
                log.warn("未找到促销类型 [{}] 的策略实现", promo.getPromoType());
                continue;
            }

            // 更新上下文中的累计优惠
            ctx.setAccumulatedDiscount(accumulatedDiscount);

            try {
                if ("BUY_GIFT".equals(promo.getPromoType())) {
                    // 买赠策略：计算赠品
                    BigDecimal giftResult = strategy.calculate(ctx, promo);
                    if (giftResult.compareTo(BigDecimal.ZERO) < 0) {
                        // 负值 = 赠品数量（取绝对值）
                        BigDecimal giftQty = giftResult.abs();
                        // 从规则中找到赠品 SKU
                        Long giftSkuId = null;
                        if (promo.getRules() != null) {
                            for (PromotionRule rule : promo.getRules()) {
                                if ("ACTION".equals(rule.getRuleType()) && rule.getGiftSkuId() != null) {
                                    giftSkuId = rule.getGiftSkuId();
                                    break;
                                }
                            }
                        }
                        result.addGift(giftSkuId, giftQty, promo.getPromoName());
                        log.info("买赠命中: {} → 赠品 SKU[{}] × {}", promo.getPromoName(), giftSkuId, giftQty);
                    }
                } else {
                    // 金额类策略：直接累加优惠
                    BigDecimal discount = strategy.calculate(ctx, promo);
                    if (discount.compareTo(BigDecimal.ZERO) > 0) {
                        accumulatedDiscount = accumulatedDiscount.add(discount);
                        result.addDiscount(discount, promo.getPromoName());
                        log.info("促销命中: {} → 优惠 {} 元", promo.getPromoName(), discount);
                    }
                }
            } catch (Exception e) {
                log.error("促销 [{}] 计算异常: {}", promo.getPromoName(), e.getMessage(), e);
            }
        }

        // 确保优惠总额不超过原始金额
        BigDecimal originalTotal = ctx.getOriginalTotal() != null ? ctx.getOriginalTotal() : BigDecimal.ZERO;
        if (result.getTotalDiscount().compareTo(originalTotal) > 0) {
            result.setTotalDiscount(originalTotal);
        }

        return result;
    }

    /**
     * 加载当前有效的促销活动
     */
    private List<Promotion> loadActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getStatus, 1)
                        .le(Promotion::getStartTime, now)
                        .ge(Promotion::getEndTime, now));
    }
}
