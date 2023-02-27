/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.modules.entity.JobEntity;

import java.util.List;
import java.util.Map;

/**
 * 定时任务
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface JobService extends IService<JobEntity> {

	PageUtils queryPage(Map<String, Object> params);

	/**
	 * 保存定时任务
	 */
	void newJob(JobEntity scheduleJob);

	/**
	 * 批量删除定时任务
	 */
	void deleteBatch(Long[] jobIds);

	List<JobEntity> queryJobByInstanceId(long instanceId);

	List<JobEntity> queryPhysicalJobByInstanceId(long instanceId);
}
