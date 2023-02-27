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
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 系统日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("sys_log")
public class LogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId
	private Long id;

	//用户名
	private String username;

	//操作实例名称
	private String instanceName;

	//登录IP
	private String logIp;

	//操作
	private String operation;

	//操作时间
	private Date operationTime;
}
