package com.meat.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.system.entity.SysDict;
import com.meat.system.entity.SysDictItem;
import java.util.List;

public interface SysDictService extends IService<SysDict> {
    List<SysDictItem> listItemsByCode(String dictCode);
}
