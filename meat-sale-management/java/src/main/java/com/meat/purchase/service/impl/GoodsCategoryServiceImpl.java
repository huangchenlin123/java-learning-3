package com.meat.purchase.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.purchase.entity.GoodsCategory;
import com.meat.purchase.mapper.GoodsCategoryMapper;
import com.meat.purchase.service.GoodsCategoryService;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {
    @Override
    public List<GoodsCategory> listTree() {
        List<GoodsCategory> all = list(new LambdaQueryWrapper<GoodsCategory>().orderByAsc(GoodsCategory::getSort));
        List<GoodsCategory> roots = all.stream().filter(c -> c.getParentId() == null || c.getParentId() == 0L).collect(Collectors.toList());
        for (GoodsCategory root : roots) buildChildren(root, all);
        return roots;
    }
    private void buildChildren(GoodsCategory parent, List<GoodsCategory> all) {
        List<GoodsCategory> children = all.stream().filter(c -> parent.getId().equals(c.getParentId())).collect(Collectors.toList());
        parent.setChildren(children);
        for (GoodsCategory child : children) buildChildren(child, all);
    }
}
