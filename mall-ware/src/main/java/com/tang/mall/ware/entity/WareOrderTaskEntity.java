package com.tang.mall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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
@TableName("wms_ware_order_task")
public class WareOrderTaskEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * order_id
	 */
	private Long orderId;
	/**
	 * order_sn
	 */
	private String orderSn;
	/**
	 * 
	 */
	private String consignee;
	/**
	 * 
	 */
	private String consigneeTel;
	/**
	 * 
	 */
	private String deliveryAddress;
	/**
	 * 
	 */
	private String orderComment;
	/**
	 * 
	 */
	private Integer paymentWay;
	/**
	 * 
	 */
	private Integer taskStatus;
	/**
	 * 
	 */
	private String orderBody;
	/**
	 * 
	 */
	private String trackingNo;
	/**
	 * create_time
	 */
	private Date createTime;
	/**
	 * 
	 */
	private Long wareId;
	/**
	 * 
	 */
	private String taskComment;

}
