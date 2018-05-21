package com.cxgm.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Order implements Serializable{
	
    private static final long serialVersionUID = 6031854054753338431L;
    
	@ApiModelProperty(name = "id",value = "主键ID",hidden = true)
    private Integer id;

	@ApiModelProperty(name = "userId",value = "用户ID",hidden = true)
    private Integer userId;

	@ApiModelProperty(name = "orderNum",value = "订单编号")
    private String orderNum;

	@ApiModelProperty(name = "status",value = "订单状态0待支付，1待配送（已支付），2配送中，3已完成，4退货，5已取消")
    private String status;

	@ApiModelProperty(name = "storeId",value = "门店ID")
    private Integer storeId;

	@ApiModelProperty(name = "payType",value = "支付方式")
    private String payType;

	@ApiModelProperty(name = "orderTime",value = "订单时间")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

	@ApiModelProperty(name = "orderAmount",value = "订单金额")
    private BigDecimal orderAmount;

	@ApiModelProperty(name = "remarks",value = "备注")
    private String remarks;
	
	@ApiModelProperty(name = "addressId",value = "收货地址ID")
    private String addressId;
	
	@ApiModelProperty(name = "receiveTime",value = "预计收货时间")
    private String receiveTime;
	
	@ApiModelProperty(name = "receipt",value = "发票实体对象")
    private Receipt receipt;
	
	@ApiModelProperty(name = "couponCodeId",value = "优惠券ID")
    private Integer couponCodeId;
	
	private List<OrderProduct>  productList;
	
	private List<OrderProductTransfer>  productDetails;
}