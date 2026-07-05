package com.meat.purchase.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.purchase.entity.GoodsSku;
import com.meat.purchase.mapper.GoodsSkuMapper;
import com.meat.purchase.service.GoodsSkuService;
import org.springframework.stereotype.Service;
@Service
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSku> implements GoodsSkuService {}
