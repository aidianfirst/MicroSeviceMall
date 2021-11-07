package com.tang.mall.product.service.impl;

import com.tang.mall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.common.utils.PageUtils;
import com.tang.common.utils.Query;

import com.tang.mall.product.dao.CategoryDao;
import com.tang.mall.product.entity.CategoryEntity;
import com.tang.mall.product.service.CategoryService;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Resource
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查询所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //父子树形结构生成，使用java8 stream过滤数据
        List<CategoryEntity> Menu = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).sorted((menu1, menu2) ->
                (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).map(menu -> {
            menu.setChildMenu(getChildren(menu, entities));
            return menu;
        }).collect(Collectors.toList());

        return Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    // 查找catelogId完整路径
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    // 级联更新全部数据
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    // 递归查找子菜单
    private List<CategoryEntity> getChildren(CategoryEntity father, List<CategoryEntity> all){
        List<CategoryEntity> child = all.stream().filter(categoryEntity ->
                categoryEntity.getParentCid().equals(father.getCatId())
        ).sorted((menu1, menu2) ->
                (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).map(menu -> {
            menu.setChildMenu( getChildren(menu, all) );
            return menu;
        }).collect(Collectors.toList());

        return child;
    }

    // 递归查找父节点id，并记录路径
    private List<Long> findParentPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity id = this.getById(catelogId);
        if (id.getParentCid() != 0){
            findParentPath(id.getParentCid(), paths);
        }
        return paths;
    }
}