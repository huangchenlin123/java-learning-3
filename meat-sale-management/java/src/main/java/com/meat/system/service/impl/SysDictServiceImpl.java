package com.meat.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.system.entity.SysDict;
import com.meat.system.entity.SysDictItem;
import com.meat.system.mapper.SysDictItemMapper;
import com.meat.system.mapper.SysDictMapper;
import com.meat.system.service.SysDictService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Resource
    private SysDictMapper sysDictMapper;

    @Resource
    private SysDictItemMapper sysDictItemMapper;

    @Override
    public List<SysDictItem> listItemsByCode(String dictCode) {
        SysDict dict = sysDictMapper.selectOne(
                new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, dictCode));
        if (dict == null) {
            throw new BusinessException("字典不存在: " + dictCode);
        }
        List<SysDictItem> items = sysDictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, dict.getId())
                        .orderByAsc(SysDictItem::getSort));
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, id));
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(java.util.Collection<?> list) {
        if (list != null && !list.isEmpty()) {
            for (Object id : list) {
                sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, id));
            }
        }
        return super.removeByIds(list);
    }
}
