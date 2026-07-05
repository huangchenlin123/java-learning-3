package com.meat.warehouse.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.warehouse.entity.Shelf;
import com.meat.warehouse.mapper.ShelfMapper;
import com.meat.warehouse.service.ShelfService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class ShelfServiceImpl extends ServiceImpl<ShelfMapper, Shelf> implements ShelfService {
    @Resource
    private ShelfMapper shelfMapper;
    @Override
    public List<Shelf> listByWarehouseId(Long warehouseId) {
        return shelfMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Shelf>()
                .eq(Shelf::getWarehouseId, warehouseId));
    }
}
