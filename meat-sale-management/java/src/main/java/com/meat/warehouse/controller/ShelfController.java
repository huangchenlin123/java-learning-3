package com.meat.warehouse.controller;
import com.meat.common.R;
import com.meat.warehouse.entity.Shelf;
import com.meat.warehouse.service.ShelfService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "货架管理")
@RestController
@RequestMapping("/api/base/shelf")
@RequiredArgsConstructor
public class ShelfController {
    private final ShelfService shelfService;
    @GetMapping @ApiOperation("货架列表")
    public R<List<Shelf>> list(@RequestParam(required = false) Long warehouseId) {
        return R.ok(warehouseId != null ? shelfService.listByWarehouseId(warehouseId) : shelfService.list());
    }
    @GetMapping("/{id}") @ApiOperation("详情")
    public R<Shelf> getById(@PathVariable Long id) { return R.ok(shelfService.getById(id)); }
    @PostMapping @ApiOperation("新增")
    public R<Void> save(@RequestBody Shelf s) { shelfService.save(s); return R.ok(); }
    @PutMapping @ApiOperation("修改")
    public R<Void> update(@RequestBody Shelf s) { shelfService.updateById(s); return R.ok(); }
    @DeleteMapping("/{ids}") @ApiOperation("删除")
    public R<Void> delete(@PathVariable String ids) { for(String sid:ids.split(",")) shelfService.removeById(Long.parseLong(sid)); return R.ok(); }
}
