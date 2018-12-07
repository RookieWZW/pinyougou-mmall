package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

public class SeckillTask {

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	
	@Scheduled(cron="0 * * * * ?")
	public void refreshSechkillGoods() {
		System.out.println("执行了任务调度"+new Date());	
		
		List ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
		
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		
		
		Criteria criteria = example.createCriteria();
		
		criteria.andStatusEqualTo("1");//审核通过
		criteria.andStockCountGreaterThan(0);//剩余库存大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
		criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间		
		if(ids.size()>0){
			criteria.andIdNotIn(ids);//排除缓存中已经存在的商品ID集合
		}
				
		List<TbSeckillGoods> seckillGoodsList= seckillGoodsMapper.selectByExample(example);		
		//装入缓存 
		for( TbSeckillGoods seckill:seckillGoodsList ){
			redisTemplate.boundHashOps("seckillGoods").put(seckill.getId(), seckill);
		}
		System.out.println("将"+seckillGoodsList.size()+"条商品装入缓存");

		
	}
	
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods() {
		
		
		List<TbSeckillGoods> seckillGoodsList= redisTemplate.boundHashOps("seckillGoods").values();
		System.out.println("执行了清除秒杀商品的任务"+new Date());
		
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			
			
			//同步到数据库
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);				
			//清除缓存
			redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
			System.out.println("秒杀商品"+seckillGoods.getId()+"已过期");
							
		}
		System.out.println("执行了清除秒杀商品的任务...end");
		
		
	}
}
