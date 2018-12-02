package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RequestMapping("/cart")
@RestController
public class CartController {
	
	@Reference(timeout=6000)
	private CartService cartService;
	
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName(); 
		String cartListString  = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if(cartListString==null || cartListString.equals("")){
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		if(username.equals("anonymousUser")){		
			return cartList_cookie;			
		}else{
			List<Cart> cartList_redis =cartService.findCartListFromRedis(username);
			if(cartList_cookie.size()>0){
				
				cartList_redis=cartService.mergeCartList(cartList_redis, cartList_cookie);	
			
				util.CookieUtil.deleteCookie(request, response, "cartList");
				
				cartService.saveCartListToRedis(username, cartList_redis); 
			}			
			return cartList_redis;			
		}	
	}

	
	
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName(); 
		System.out.println("当前登录用户："+username);
		try {
			List<Cart> cartList = findCartList();
			
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			if(username.equals("anonymousUser")) {
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24 ,"UTF-8");
				System.out.println("向cookie存入数据");

			}else {
				cartService.saveCartListToRedis(username, cartList);		
			}
			return new Result(true, "添加成功");
		} catch (RuntimeException e) {
			e.printStackTrace();
			return new Result(false, e.getMessage());
		}catch(Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
