package com.meat.sale.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.sale.entity.Customer;
import com.meat.sale.mapper.CustomerMapper;
import com.meat.sale.service.CustomerService;
import org.springframework.stereotype.Service;
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {}
