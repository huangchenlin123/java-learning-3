package com.meat.sale.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.sale.entity.Customer;
import com.meat.sale.service.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Api(tags = "客户管理")
@RestController
@RequestMapping("/api/sale/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    @GetMapping @ApiOperation("分页查询")
    public R<Page<Customer>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(required = false) String keyword) {
        Page<Customer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(Customer::getCustomerName, keyword);
        w.orderByDesc(Customer::getCreateTime);
        return R.ok(customerService.page(page, w));
    }
    @GetMapping("/all") @ApiOperation("全部客户")
    public R<java.util.List<Customer>> all() { return R.ok(customerService.list()); }
    @PostMapping @ApiOperation("新增") public R<Void> save(@RequestBody Customer c) { customerService.save(c); return R.ok(); }
    @PutMapping("/{id}") @ApiOperation("修改") public R<Void> update(@PathVariable Long id, @RequestBody Customer c) { c.setId(id); customerService.updateById(c); return R.ok(); }
    @DeleteMapping("/{id}") @ApiOperation("删除") public R<Void> delete(@PathVariable Long id) { customerService.removeById(id); return R.ok(); }
}
