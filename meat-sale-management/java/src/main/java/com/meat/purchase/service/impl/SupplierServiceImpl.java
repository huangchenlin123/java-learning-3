package com.meat.purchase.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.purchase.entity.Supplier;
import com.meat.purchase.mapper.SupplierMapper;
import com.meat.purchase.service.SupplierService;
import org.springframework.stereotype.Service;
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {}
