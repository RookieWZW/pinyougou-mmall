package com.pinyougou.manager.controller;
import java.util.Arrays;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	@Autowired
	private Destination queueSolrDestination;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private Destination queueSolrDeleteDestination;
	
	@Autowired
	private Destination topicPageDestination;
	
	@Autowired
	private Destination topicPageDeleteDestination;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			
			jmsTemplate.send(queueSolrDeleteDestination,new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {	
					return session.createObjectMessage(ids);
				}

			});
			jmsTemplate.send(topicPageDeleteDestination,new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {	
					return session.createObjectMessage(ids);
				}

			});
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status) {
		try {
			goodsService.updateStatus(ids, status);
			if(status.equals("1")) {
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				if(itemList.size()>0) {
					final String jsonString = JSON.toJSONString(itemList);
					jmsTemplate.send(queueSolrDestination,new MessageCreator() {
						public Message createMessage(Session session)throws JMSException{
							System.out.println("test");
							return session.createTextMessage(jsonString);
						}
					});
					
					for(final Long goodsId:ids) {
						jmsTemplate.send(topicPageDestination,new MessageCreator() {
							@Override
							public Message createMessage(Session session) throws JMSException {							
								return session.createTextMessage(goodsId+"");
							}

						});
					}
				}
					

				
			}
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}

	}
	
	
	
	@RequestMapping("/getHtml")
	public void getHtml(Long goodsId) {
		//itemPageService.genItemHtml(goodsId);
	}
	
}
