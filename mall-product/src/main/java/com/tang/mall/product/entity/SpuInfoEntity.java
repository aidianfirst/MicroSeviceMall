package com.tang.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu
 * 
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-28 19:22:36
 */
@Data
@TableName("pms_spu_info")
public class SpuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@TableId
	private Long id;
	
	private String spuName;
	
	private String spuDescription;
	
	private Long catelogId;

	private Long brandId;
	
	private BigDecimal weight;
	
	private Integer publishStatus;
	
	private Date createTime;
	
	private Date updateTime;

}
