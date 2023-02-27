/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.opengauss.batman.common.Const;
import org.opengauss.batman.common.ErrorCode;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.Query;
import org.opengauss.batman.modules.dao.JobDao;
import org.opengauss.batman.modules.entity.JobEntity;
import org.opengauss.batman.modules.execution.BackupManager;
import org.opengauss.batman.modules.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("jobService")
public class JobServiceImpl extends ServiceImpl<JobDao, JobEntity> implements JobService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobServiceImpl.class);

	@Autowired
    private BackupManager backupManager;

	/**
	 * 项目启动时，初始化定时器
	 */
	@PostConstruct
	public void init() {
		List<JobEntity> jobs = this.list();
		backupManager.init(jobs);
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		String jobName = (String)params.get("jobname");
		IPage<JobEntity> page = this.page(new Query<JobEntity>().getPage(params),
			new QueryWrapper <JobEntity>().like(StringUtils.isNotBlank(jobName),"job_name", jobName)
		);

		return new PageUtils(page);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void newJob(JobEntity job) {
		checkCorrelativeJob(job);
		job.setCreateTime(new Date());
        baseMapper.insertNewJob(job);
		long jobId = baseMapper.queryJobIdByName(job.getInstanceId(), job.getName());
		job.setId(jobId);
		backupManager.scheduleBackupJob(job);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] jobIds) {
		List<JobEntity> jobs = baseMapper.queryJobsByIds(Arrays.asList(jobIds));
		backupManager.deleteBackupJobs(jobs);
    	this.removeByIds(Arrays.asList(jobIds));
	}

	@Override
	public List<JobEntity> queryJobByInstanceId(long instanceId) {
		return baseMapper.queryJobsByInstanceId(instanceId);
	}

	@Override
	public List<JobEntity> queryPhysicalJobByInstanceId(long instanceId) {
		return baseMapper.queryPhysicalJobByInstanceId(instanceId);
	}
	/**
	 * 每一个实例只能有一个全量物理备份任务和增量物理备份任务
	 * 增量物理备份必须依赖一个全量物理备份
	 * @param entity 待检测的新建备份任务
	 */
	private synchronized void checkCorrelativeJob(JobEntity entity) {
		List<JobEntity> existJobs = baseMapper.queryJobsByInstanceId(entity.getInstanceId());
		if (CollectionUtils.isEmpty(existJobs)) {
			if (StringUtils.equalsIgnoreCase(Const.INCREMENT_BACKUP, entity.getOption())) {
				LOGGER.error("no full physical backup task exist, can not create increment backup task");
				throw new BatException(ErrorCode.NO_FULL_BACKUP_ERROR);
			}
		}

		List<JobEntity> sameMethodJobs = existJobs.stream().filter(
			job -> StringUtils.equals(job.getOption(), entity.getOption())
				&& StringUtils.equals(job.getType(), entity.getType()))
			.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(sameMethodJobs)) {
			LOGGER.error("{} backup task already exist, can not create task more than once", entity.getOption());
			throw new BatException(ErrorCode.DUPLICATE_JOB_ERROR);
		}
	}
}
