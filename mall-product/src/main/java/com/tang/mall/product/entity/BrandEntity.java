package com.tang.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

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

	@NotNull(message = "修改必须指定id")
	@Null(message = "新增不能指定id")
	@TableId
	private Long brandId;

	@NotBlank(message = "品牌名必须填写")
	private String name;

	@NotEmpty
	@URL(message = "必须是合法url")
	private String logo;

	private String descript;

	@NotEmpty
	private Integer showStatus;

	@NotEmpty
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母")
	private String firstLetter;

	@NotNull
	@Min(value = 0, message = "排序值必须大于0")
	private Integer sort;

}
