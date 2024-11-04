package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.system.domain.entity.SysCategory;
import com.och.system.domain.query.category.SysCategoryAddQuery;
import com.och.system.domain.query.category.SysCategoryQuery;
import com.och.system.mapper.SysCategoryMapper;
import com.och.system.service.ISysCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 分类配置表(SysCategory)表服务实现类
 *
 * @author danmo
 * @since 2023-10-31 11:06:55
 */
@Service
public class SysCategoryServiceImpl extends BaseServiceImpl<SysCategoryMapper, SysCategory> implements ISysCategoryService {

    @Override
    public void add(SysCategoryAddQuery query) {
        SysCategory lfsCategory = checkName(query);
        if (null != lfsCategory) {
            throw new CommonException("名称已存在！");
        }
        SysCategory category = new SysCategory();
        BeanUtil.copyProperties(query, category);
        save(category);
    }


    @Override
    public void edit(SysCategoryAddQuery query) {
        //判断是否存在相同的名称
        SysCategory lfsCategory = checkName(query);
        if (null != lfsCategory) {
            throw new CommonException("名称已存在！");
        }
        SysCategory category = new SysCategory();
        BeanUtil.copyProperties(query, category);
        category.setId(query.getId());
        updateById(category);
    }

    @Override
    public void delete(SysCategoryQuery query) {
        if (query.getId() == null) {
            throw new CommonException("分类ID不能为空");
        }
        SysCategory category = getById(query.getId());
        if (Objects.nonNull(category) && category.getFlag() == 1) {
            throw new CommonException("分类不可删除");
        }
        SysCategory delCategory = new SysCategory();
        delCategory.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        update(delCategory, new LambdaQueryWrapper<SysCategory>()
                .and(item -> item.eq(SysCategory::getId, query.getId()).or().eq(SysCategory::getParentId, query.getId())));
    }

    @Override
    public SysCategory getDetail(Long id) {
        return getById(id);
    }

    @Override
    public List<Tree<Long>> treeList(SysCategoryQuery query) {
        List<SysCategory> list = new ArrayList<>();
        SysCategory weCategoryVo = new SysCategory();
        weCategoryVo.setId(0L);
        weCategoryVo.setName("默认分组");
        weCategoryVo.setFlag(1);
        weCategoryVo.setParentId(0L);
        list.add(0, weCategoryVo);

        List<SysCategory> weCategories = baseMapper.categoryList(query.getType());
        list.addAll(weCategories);

        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        //自定义属性名 都要默认值的
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setWeightKey("id");
        treeNodeConfig.setParentIdKey("parentId");
        return TreeUtil.build(list, 0L, treeNodeConfig, ((category, treeNode) -> {
            treeNode.setId(category.getId());//id
            treeNode.setParentId(category.getParentId());//父id
            treeNode.putExtra("title", category.getName());
            treeNode.putExtra("type", category.getType());
            treeNode.putExtra("flag", category.getFlag());
            treeNode.putExtra("key", category.getId());
        }));
    }

    private SysCategory checkName(SysCategoryAddQuery query) {
        //判断是否存在相同的名称
        return this.getOne(
                new LambdaQueryWrapper<SysCategory>().eq(SysCategory::getType, query.getType())
                        .eq(SysCategory::getName, query.getName()).eq(SysCategory::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex())
                        .eq(SysCategory::getParentId, query.getParentId()));
    }
}

