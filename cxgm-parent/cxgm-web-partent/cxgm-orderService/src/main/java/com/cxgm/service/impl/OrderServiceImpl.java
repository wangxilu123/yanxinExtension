package com.cxgm.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.cxgm.common.ClientCustomSSL;
import com.cxgm.common.CodeUtil;
import com.cxgm.common.DateUtil;
import com.cxgm.common.RequestHandler;
import com.cxgm.dao.CouponCodeMapper;
import com.cxgm.dao.CouponMapper;
import com.cxgm.dao.OrderMapper;
import com.cxgm.dao.OrderProductMapper;
import com.cxgm.dao.ProductMapper;
import com.cxgm.dao.ReceiptMapper;
import com.cxgm.dao.ShopCartMapper;
import com.cxgm.domain.CategoryAndAmount;
import com.cxgm.domain.CouponCode;
import com.cxgm.domain.CouponDetail;
import com.cxgm.domain.Order;
import com.cxgm.domain.OrderExample;
import com.cxgm.domain.OrderProduct;
import com.cxgm.domain.OrderProductTransfer;
import com.cxgm.domain.Product;
import com.cxgm.domain.ProductTransfer;
import com.cxgm.domain.ShopCartExample;
import com.cxgm.service.OrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Primary
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private OrderProductMapper orderProductMapper;

	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private CouponMapper couponMapper;

	@Autowired
	private CouponCodeMapper couponCodeMapper;

	@Autowired
	private ReceiptMapper receiptMapper;

	@Autowired
	private ShopCartMapper shopCartMapper;

	@Override
	public Integer addOrder(Order order) {
		String orderNum=DateUtil.formatDateTime2()+CodeUtil.genCodes(6);
		order.setOrderTime(new Date());
		order.setStatus("0");
		order.setOrderNum(orderNum);
		orderMapper.insert(order);

		for (OrderProduct orderProduct : order.getProductList()) {

			orderProduct.setOrderId(order.getId());
			orderProduct.setCreateTime(new Date());
			orderProductMapper.insert(orderProduct);
			// 修改销量
			Product product = productMapper.findProductByGoodCode(orderProduct.getGoodCode());

			if(product!=null){
				product.setSales(product.getSales()!=null?product.getSales():0 + (orderProduct.getProductNum()!=null?orderProduct.getProductNum():0));
			}
			

			productMapper.update(product);

			// 从购物车里面移除商品
			ShopCartExample example = new ShopCartExample();

			example.createCriteria().andGoodCodeEqualTo(orderProduct.getGoodCode()).andShopIdEqualTo(order.getStoreId())
					.andUserIdEqualTo(order.getUserId());

			shopCartMapper.deleteByExample(example);

		}
		// 发票信息
		if(order.getReceipt()!=null){
			order.getReceipt().setCreateTime(new Date());
			receiptMapper.insert(order.getReceipt());
		}
		// 更改优惠券信息
		if(order.getCouponCodeId()!=null){
			CouponCode couponCode = couponCodeMapper.select((long) order.getCouponCodeId());

			if(couponCode!=null){
				couponCode.setStatus(1);

				couponCodeMapper.update(couponCode);
			}
		}
		return order.getId();
	}

	@Override
	public Integer updateOrder(Order order) {

		OrderExample example = new OrderExample();

		example.createCriteria().andUserIdEqualTo(order.getUserId()).andIdEqualTo(order.getId());

		return orderMapper.updateByExample(order, example);
	}
	
	@Override
	public Integer returnMoney(Integer orderId) {
		
		/*//根据

		OrderExample example = new OrderExample();

		example.createCriteria().andUserIdEqualTo(order.getUserId()).andIdEqualTo(order.getId());*/

		return null;
	}


	@Override
	public PageInfo<Order> orderList(Integer pageNum, Integer pageSize, Integer userId, String status) {

		PageHelper.startPage(pageNum, pageSize);

		OrderExample example = new OrderExample();
		if ("".equals(status) == false && status != null) {
			example.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(status);
		} else {
			example.createCriteria().andUserIdEqualTo(userId);
		}
		List<Order> list = orderMapper.selectByExample(example);

		for (Order order : list) {
			// 根据orderId查询订单详情信息

			List<OrderProductTransfer> productList = orderProductMapper.selectOrderDetail(order.getId());

			for (OrderProductTransfer orderDetail : productList) {

				BigDecimal price = orderDetail.getPrice();

				BigDecimal num = new BigDecimal(orderDetail.getProductNum());

				orderDetail.setAmount(price.multiply(num));

			}

			order.setProductDetails(productList);

		}

		PageInfo<Order> page = new PageInfo<Order>(list);

		return page;
	}

	@Override
	public Order findById(Integer orderId) {

		OrderExample example = new OrderExample();

		example.createCriteria().andIdEqualTo(orderId);
		List<Order> list = orderMapper.selectByExample(example);
		if (list.size() != 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<CouponDetail> checkCoupons(Integer userId, List<OrderProduct> productList,
			List<CategoryAndAmount> amountList,BigDecimal totalAmount) {
		// 根据商品ID查询商品分类
		List<CouponDetail> newList = new ArrayList<CouponDetail>();

		for (OrderProduct orderProduct : productList) {
			ProductTransfer productTransfer = productMapper.findById((long) orderProduct.getProductId());

			// 该商品的总价
			BigDecimal amount=new BigDecimal(0);
			if(orderProduct.getProductNum()!=null&&productTransfer.getPrice()!=null){
				 amount = productTransfer.getPrice().multiply(new BigDecimal(orderProduct.getProductNum()));
			}
			// 根据商品ID查询优惠券

			HashMap<String, Object> map = new HashMap<String, Object>();

			map.put("userId", userId);
			map.put("amount", amount);
			map.put("productId", orderProduct.getProductId());
			List<CouponDetail> list = couponMapper.findCouponsByProduct(map);

			newList.addAll(list);
		}

		// 根据商品二级分类及金额查询优惠券

		HashMap<String, Object> map1 = new HashMap<String, Object>();

		map1.put("userId", userId);
		map1.put("amountList", amountList);
		if(amountList!=null&&amountList.size()!=0){
			List<CouponDetail> list1 = couponMapper.findCouponsByProduct(map1);
			
			newList.addAll(list1);
		}
		
		//根据全品类查询优惠券
		
		HashMap<String, Object> map2 = new HashMap<String, Object>();

		map2.put("userId", userId);
		map2.put("totalAmount", totalAmount);
		
		List<CouponDetail> list2 = couponMapper.findCouponsByProduct(map2);
			
		newList.addAll(list2);
		newList = new ArrayList<CouponDetail>(new LinkedHashSet<>(newList));

		return newList;
	}
	//微信退款业务逻辑
	public void wechatRefund() {
		String out_refund_no = "654232";// 退款单号
		String out_trade_no = "12355";// 订单号
		String total_fee = "1";// 总金额
		String refund_fee = "1";// 退款金额
		String nonce_str = "4232343765";// 随机字符串
		String appid = "";
		String appsecret = "";
		String mch_id = "";
		String op_user_id = "";//就是MCHID
		String partnerkey = "";//商户平台上的那个KEY
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("out_refund_no", out_refund_no);
		packageParams.put("total_fee", total_fee);
		packageParams.put("refund_fee", refund_fee);
		packageParams.put("op_user_id", op_user_id);

		RequestHandler reqHandler = new RequestHandler(
				null, null);
		reqHandler.init(appid, appsecret, partnerkey);

		String sign = reqHandler.createSign(packageParams);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>"
				+ mch_id + "</mch_id>" + "<nonce_str>" + nonce_str
				+ "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
				+ "<out_trade_no>" + out_trade_no + "</out_trade_no>"
				+ "<out_refund_no>" + out_refund_no + "</out_refund_no>"
				+ "<total_fee>" + total_fee + "</total_fee>"
				+ "<refund_fee>" + refund_fee + "</refund_fee>"
				+ "<op_user_id>" + op_user_id + "</op_user_id>" + "</xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
		try {
			String s= ClientCustomSSL.doRefund(createOrderURL, xml);
			System.out.println(s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
