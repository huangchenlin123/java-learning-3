package com.meat.sale.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meat.sale.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {}
