package com.tang.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Ʒ
 * 
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-28 19:22:36
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Ʒ
	 */
	@TableId
	private Long brandId;
	/**
	 * Ʒ
	 */
	private String name;
	/**
	 * Ʒ
	 */
	private String logo;
	/**
	 * 
	 */
	private String descript;
	/**
	 * 
	 */
	private Integer showStatus;
	/**
	 * 
	 */
	private String firstLetter;
	/**
	 * 
	 */
	private Integer sort;

}
