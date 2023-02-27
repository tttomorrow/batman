/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.controller;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.opengauss.batman.common.annotation.SysLog;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.R;
import org.opengauss.batman.common.validator.ValidatorUtils;
import org.opengauss.batman.modules.entity.JobEntity;
import org.opengauss.batman.modules.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 定时任务
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/sys/job")
public class JobController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

	@Autowired
	private JobService jobService;
	
	/**
	 * 定时任务历史
	 */
	@RequestMapping("/list")
	public R list(@RequestParam Map<String, Object> params){
		PageUtils page = jobService.queryPage(params);

		return R.ok().put("page", page);
	}
	
	/**
	 * 定时任务信息
	 */
	@RequestMapping("/info/{jobId}")
	public R info(@PathVariable("jobId") Long jobId){
		JobEntity schedule = jobService.getById(jobId);
		
		return R.ok().put("schedule", schedule);
	}
	
	/**
	 * 保存定时任务
	 */
	@SysLog("创建备份计划")
	@RequestMapping("/new")
	public R save(@RequestBody JobEntity scheduleJob){
		ValidatorUtils.validateEntity(scheduleJob);
		try {
			jobService.newJob(scheduleJob);
			return R.ok();
		} catch (BatException e) {
			LOGGER.error("create new job exception: {}", ExceptionUtils.getStackTrace(e));
			return R.error(e.getCode(), e.getMessage());
		}
	}

	/**
	 * 删除定时任务
	 */
	@SysLog("删除备份计划")
	@RequestMapping("/delete")
	public R delete(@RequestBody Long[] jobIds){
		try {
			jobService.deleteBatch(jobIds);
			return R.ok();
		} catch (BatException e) {
			return R.error(e.getCode(), e.getMessage());
		}
	}
}
