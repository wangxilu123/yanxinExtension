package com.cxgm.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.cxgm.dao.ProductMapper;
import com.cxgm.domain.ProductTransfer;
import com.cxgm.domain.ShopCategory;
import com.cxgm.service.HomePageService;

@Primary
@Service
public class HomePageServiceImpl implements HomePageService {

	@Autowired
	private ProductMapper productDao;
	
	@Override
	public List<ProductTransfer> findListAllWithCategory(Map<String,Object> map){
		return productDao.findListAllWithCategory(map);
	}

	@Override
	public List<ShopCategory> findShopOneCategory(Integer shopId) {
		
		return productDao.findShopCategory(shopId);
	}
}