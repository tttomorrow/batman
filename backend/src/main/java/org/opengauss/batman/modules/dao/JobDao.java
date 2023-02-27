/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.batman.modules.entity.JobEntity;

import java.util.List;
import java.util.Map;

/**
 * 定时任务
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface JobDao extends BaseMapper<JobEntity> {
	
	/**
	 * 批量更新状态
	 */
	int updateBatch(Map<String, Object> map);

	long insertNewJob(JobEntity entity);

	long queryJobIdByName(long instanceId, String jobName);

	JobEntity queryJobById(long jobId);

	List<JobEntity> queryJobsByIds(List<Long> jobIds);

	List<JobEntity> queryJobsByInstanceId(long instanceId);

	List<JobEntity> queryPhysicalJobByInstanceId(long instanceId);
}
