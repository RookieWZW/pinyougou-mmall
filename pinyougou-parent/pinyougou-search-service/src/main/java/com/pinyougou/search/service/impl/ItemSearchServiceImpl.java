package com.pinyougou.search.service.impl;

import java.util.Map;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=3000)
public class ItemSearchServiceImpl implements ItemSearchService{
	
	
	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map<String, Object> search(Map searchMap) {
		// TODO Auto-generated method stub
		Map<String,Object> map = new HashMap<>();
		
		map.putAll(searchList(searchMap));
		
		return map;
	}
	
	private Map searchList(Map searchMap) {
		Map map = new HashMap();
		
		HighlightQuery query = new SimpleHighlightQuery();
		
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");
		
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀 
		highlightOptions.setSimplePostfix("</em>");//高亮后缀
		query.setHighlightOptions(highlightOptions);//设置高亮选项
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		
		query.addCriteria(criteria);
		
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		
		
		for (HighlightEntry<TbItem>  h : page.getHighlighted()) {
			TbItem item = h.getEntity();
			
			if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0) {
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
			}
		}
		
		map.put("rows", page.getContent());
		return map;
		
	}
	

}
