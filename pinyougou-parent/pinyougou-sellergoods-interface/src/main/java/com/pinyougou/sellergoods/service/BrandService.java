package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {
	
	List<TbBrand> findAll();
	
	public PageResult findPage(int pageNum,int pageSize);
	
	public void add(TbBrand brand);
	
	
	public void update(TbBrand brand);
	
	public TbBrand findOne(Long id);
	
}
