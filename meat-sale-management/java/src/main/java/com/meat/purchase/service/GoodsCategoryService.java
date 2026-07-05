package com.meat.purchase.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.purchase.entity.GoodsCategory;
import java.util.List;
public interface GoodsCategoryService extends IService<GoodsCategory> {
    List<GoodsCategory> listTree();
}
