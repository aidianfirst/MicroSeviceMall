package com.tang.mall.product.service.impl;

import com.tang.mall.product.service.CategoryBrandRelationService;
import com.tang.mall.product.vo.CateJsonVo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.common.utils.Query;

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

    @Caching(evict = {
            @CacheEvict(value = {"category"}, key = "'getLevel1Menu'"),
            @CacheEvict(value = {"category"}, key = "'getCateJson'")
    })
    @Override
    // 级联更新全部数据
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Menu() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public Map<String, List<CateJsonVo>> getCateJson() {
        Map<String, List<CateJsonVo>> cateJsonWithSpringCache = getCateJsonWithSpringCache();
        return cateJsonWithSpringCache;
    }

    public Map<String, List<CateJsonVo>> getCateJsonWithSpringCache() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> categoryLevel1 = getBypCid(0L, categoryEntities);
        Map<String, List<CateJsonVo>> collect = categoryLevel1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 二级放一级
            List<CategoryEntity> categoryLevel2 = getBypCid(v.getCatId(), categoryEntities);
            List<CateJsonVo> cateJsonVoList = categoryLevel2.stream().map(item -> {
                CateJsonVo cateJsonVo = new CateJsonVo();
                cateJsonVo.setCatelog1Id(item.getParentCid());
                cateJsonVo.setId(item.getCatId());
                cateJsonVo.setName(item.getName());
                // 三级放二级
                List<CategoryEntity> categoryLevel3 = getBypCid(item.getCatId(), categoryEntities);
                List<CateJsonVo.Catelog3List> catelog3Lists = categoryLevel3.stream().map(categoryEntity -> {
                    CateJsonVo.Catelog3List catelog3List = new CateJsonVo.Catelog3List();
                    catelog3List.setCatelog2Id(categoryEntity.getParentCid());
                    catelog3List.setId(categoryEntity.getCatId());
                    catelog3List.setName(categoryEntity.getName());
                    return catelog3List;
                }).collect(Collectors.toList());
                cateJsonVo.setCatelog3List(catelog3Lists);
                return cateJsonVo;
            }).collect(Collectors.toList());
            return cateJsonVoList;
        }));
        return collect;
    }

    private List<CategoryEntity> getBypCid(Long pCid, List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(item ->
            item.getParentCid().equals(pCid)
        ).collect(Collectors.toList());
        return collect;
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