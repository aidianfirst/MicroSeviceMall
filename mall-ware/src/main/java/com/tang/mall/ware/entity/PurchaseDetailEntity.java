package com.tang.mall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:32:25
 */
@Data
@TableName("wms_purchase_detail")
public class PurchaseDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long purchaseId;
	/**
	 * 
	 */
	private Long skuId;
	/**
	 * 
	 */
	private Integer skuNum;
	/**
	 * 
	 */
	private BigDecimal skuPrice;
	/**
	 * 
	 */
	private Long wareId;
	/**
	 * ״̬[0
	 */
	private Integer status;

}
