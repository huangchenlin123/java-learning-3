package com.meat.warehouse.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.warehouse.entity.Shelf;
import java.util.List;
public interface ShelfService extends IService<Shelf> {
    List<Shelf> listByWarehouseId(Long warehouseId);
}
