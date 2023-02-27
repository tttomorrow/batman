/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("sys_job")
public class JobEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 任务id
	 */
	@TableId
	private Long id;

	/**
	 * 作业执行的实例ID
	 */
	@Positive(message = "实例ID不能为空")
	private Long instanceId;

	/**
	 * 作业名称
	 */
	@NotBlank(message="任务名称不能为空")
	private String name;
	
	/**
	 * 作业类型：物理备份（physical）、逻辑备份(logical)、恢复(restore)
	 */
	private String type;

	/**
	 * 若作业类型为物理备份或逻辑备份，则表示全量备份或增量备份
	 */
	private String option;

	/**
	 * 调度周期，多长时间调度一次
	 */
	private String schedule;

	/**
	 * 作业启动时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startTime;

	/**
	 * 作业创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	/**
	 * 作业是否激活
	 */
	private boolean active;

	/**
	 * 备注
	 */
	private String remark;
}
