package com.pinyougou.search.service.impl;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public Map search(Map searchMap) {
		// TODO Auto-generated method stub
		Map map = new HashMap();
		map.putAll(searchList(searchMap));
		searchList(searchMap);

		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);

		String category = (String) searchMap.get("category");
		if (!"".equals(category)) {
			map.putAll(searchBrandAndSpecList(category));
		} else {
			if (categoryList.size() > 0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		return map;
	}

	private Map searchList(Map searchMap) {
		Map map = new HashMap();

		HighlightQuery query = new SimpleHighlightQuery();

		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");

		highlightOptions.setSimplePrefix("<em style='color:red'>");// 高亮前缀
		highlightOptions.setSimplePostfix("</em>");// 高亮后缀
		query.setHighlightOptions(highlightOptions);// 设置高亮选项
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

		query.addCriteria(criteria);

		if (!"".equals(searchMap.get("category"))) {
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(criteria);
			query.addFilterQuery(filterQuery);

		}

		if (!"".equals(searchMap.get("brand"))) {// 如果用户选择了品牌
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}

		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {

				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);

			}

		}

		// *********** 获取高亮结果集 ***********
		// 高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		// 高亮入口集合(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : entryList) {
			// 获取高亮列表(高亮域的个数)
			List<Highlight> highlightList = entry.getHighlights();
			/*
			 * for(Highlight h:highlightList){ List<String> sns =
			 * h.getSnipplets();//每个域有可能存储多值 System.out.println(sns); }
			 */
			if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		return map;

	}

	private List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList();

		Query query = new SimpleQuery("*:*");

		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

		query.addCriteria(criteria);

		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");

		query.setGroupOptions(groupOptions);

		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

		List<GroupEntry<TbItem>> content = groupEntries.getContent();

		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());
		}

		return list;

	}

	private Map searchBrandAndSpecList(String category) {

		Map map = new HashMap();

		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

		if (typeId != null) {
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);

			map.put("brandList", brandList);

			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);

			map.put("specList", specList);
		}

		return map;
	}

}
