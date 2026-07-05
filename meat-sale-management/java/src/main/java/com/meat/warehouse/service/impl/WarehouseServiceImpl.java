package com.meat.warehouse.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.warehouse.entity.Warehouse;
import com.meat.warehouse.mapper.WarehouseMapper;
import com.meat.warehouse.service.WarehouseService;
import org.springframework.stereotype.Service;
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {}
