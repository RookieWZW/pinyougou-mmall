package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

public interface CartService {
	
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num); 
	
	
	public List<Cart> findCartListFromRedis(String username);
	
	
	public void saveCartListToRedis(String username,List<Cart> cartList);
	
	
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
	
	
}
