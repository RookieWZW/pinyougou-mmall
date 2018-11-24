package com.pinyougou.page.service.impl;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbUserExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class ItemPageServiceImpl implements ItemPageService {

	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public boolean genItemHtml(Long goodsId) {
		// TODO Auto-generated method stub
		try {
			Configuration configuration = freeMarkerConfig.getConfiguration();
			
			Template template = configuration.getTemplate("item.ftl");
			
			Map dataModel = new HashMap<>();
			
			
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			
			dataModel.put("goods", goods);
			
			
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			
			dataModel.put("goodsDesc", goodsDesc);
			
			
			String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			dataModel.put("itemCat1", itemCat1);
			dataModel.put("itemCat2", itemCat2);
			dataModel.put("itemCat3", itemCat3);

			
			TbItemExample example = new TbItemExample();
			
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			
			criteria.andStatusEqualTo("1");
			criteria.andGoodsIdEqualTo(goodsId);
			example.setOrderByClause("is_default desc");
			
			List<TbItem> itemList = itemMapper.selectByExample(example);
			dataModel.put("itemList",itemList);
			
			
			String dir = pagedir+goodsId+".html";
			
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dir), "UTF-8");
			PrintWriter printWriter = new PrintWriter(writer);
			
			template.process(dataModel, printWriter);

			
			printWriter.close();
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

}
