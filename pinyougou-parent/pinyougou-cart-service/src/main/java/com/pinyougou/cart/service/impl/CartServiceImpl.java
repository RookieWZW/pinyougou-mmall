package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;


@Service
public class CartServiceImpl  implements CartService{

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbItemMapper itemMapper;
	//1.根据商品SKU ID查询SKU商品信息
			//2.获取商家ID		
			//3.根据商家ID判断购物车列表中是否存在该商家的购物车		
			//4.如果购物车列表中不存在该商家的购物车
			//4.1 新建购物车对象
			//4.2 将新建的购物车对象添加到购物车列表		
			//5.如果购物车列表中存在该商家的购物车		
			// 查询购物车明细列表中是否存在该商品
			//5.1. 如果没有，新增购物车明细		
			//5.2. 如果有，在原购物车明细上添加数量，更改金额

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		// TODO Auto-generated method stub
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item==null) {
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")) {
			throw new RuntimeException("商品不存在");
		}
		
		String sellerId = item.getSellerId();
		
		Cart cart = searchCartBySellerId(cartList,sellerId);
		
		if(cart==null) {
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			TbOrderItem orderItem = createOrderItem(item,num);
			List orderItemList = new ArrayList();
			
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			
			cartList.add(cart);
		}else {
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			if(orderItem==null) {
				orderItem= createOrderItem(item,num);
				cart.getOrderItemList().add(orderItem);
			}else {
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
				if(orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);
				}
				if(cart.getOrderItemList().size()==0) {
					cartList.remove(cart);
				}
			}
		}
		
	
		return cartList;
	}
	
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		
		
		for (Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		
		return null;
	}
	
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId) {
		
		for (TbOrderItem tbOrderItem : orderItemList) {
			if(tbOrderItem.getItemId().longValue()==itemId.longValue()) {
				return tbOrderItem;
			}
		}
		
		return null;
	}
	
	
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		if(num<=0) {
			throw new RuntimeException("数量非法");
		}
		TbOrderItem orderItem = new TbOrderItem();
		
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;

		
		
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		// TODO Auto-generated method stub
		
		System.out.println("从redis中提取购物车数据....."+username);
		
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		
		if(cartList == null) {
			cartList = new ArrayList();
		}
		
		
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		// TODO Auto-generated method stub
		System.out.println("向redis存入购物车数据....."+username);
		
		redisTemplate.boundHashOps("cartList").put(username, cartList);
		
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		// TODO Auto-generated method stub
		System.out.println("合并购物车");
		
		for (Cart cart : cartList2) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				
				cartList1= addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
			}
		}
		return cartList1;
	}

	
	
}
