package com.pinyougou.sellergoods.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;


@Service
public class BrandServiceImpl implements BrandService{

	
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {
		// TODO Auto-generated method stub
		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		PageHelper.startPage(pageNum, pageSize);
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		
		return new PageResult(page.getTotal(),page.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		// TODO Auto-generated method stub
		brandMapper.insert(brand);
	}

	@Override
	public void update(TbBrand brand) {
		// TODO Auto-generated method stub
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		// TODO Auto-generated method stub
		return brandMapper.selectByPrimaryKey(id);
	}
	
}
