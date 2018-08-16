package com.pinyougou.manager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {

	
	@Reference
	private BrandService brandService;
	
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}
	
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int size) {
		return brandService.findPage(page, size);
	}
	
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand) {
		try {
			brandService.add(brand);
			return new Result(true,"增加成功");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new Result(false,"增加失败");
		}
	}
	
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		try {
			brandService.update(brand);
			
			return new Result(true,"修改成功");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new Result(false,"修改失败");
		}
	}
	
	
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}
	
	
	
	
}
