package com.tang.mall.member.entity;

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
 * @date 2021-10-29 15:26:21
 */
@Data
@TableName("ums_member_level")
public class MemberLevelEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private String name;
	/**
	 * 
	 */
	private Integer growthPoint;
	/**
	 * 
	 */
	private Integer defaultStatus;
	/**
	 * 
	 */
	private BigDecimal freeFreightPoint;
	/**
	 * ÿ
	 */
	private Integer commentGrowthPoint;
	/**
	 * 
	 */
	private Integer priviledgeFreeFreight;
	/**
	 * 
	 */
	private Integer priviledgeMemberPrice;
	/**
	 * 
	 */
	private Integer priviledgeBirthday;
	/**
	 * 
	 */
	private String note;

}
