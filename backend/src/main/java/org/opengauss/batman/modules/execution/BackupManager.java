/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.execution;

import com.jcraft.jsch.Session;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opengauss.batman.common.Const;
import org.opengauss.batman.common.ErrorCode;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.SSHUtils;
import org.opengauss.batman.modules.entity.InstanceEntity;
import org.opengauss.batman.modules.entity.JobEntity;
import org.opengauss.batman.modules.entity.TaskEntity;
import org.opengauss.batman.modules.service.InstanceService;
import org.opengauss.batman.modules.utils.PathUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 定时任务工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component
public class BackupManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupManager.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private InstanceService instanceService;

    private final Map<Long, BackupJob> scheduledJobs = new ConcurrentHashMap<>();

    public void init(List<JobEntity> jobs) {
        if (CollectionUtils.isNotEmpty(jobs)) {
            jobs.forEach(this::scheduleBackupJobsIfNeed);
        }
    }

    /**
     * 创建定时任务
     */
    public synchronized void scheduleBackupJob(JobEntity entity) {
        try {
            InstanceEntity instance = instanceService.getInstanceById(entity.getInstanceId());
            String password = instanceService.getPlainPassword(instance.getOsPassword(), instance.getId());
            instance.setOsPassword(password);
            makeJobPathIfAbsent(instance, entity);
            modifyGUCParameterIfNeed(instance, entity);
            BackupJob job = buildBackupJob(entity);
            scheduler.scheduleJob(job.getJobDetail(), job.getJobTrigger());
            scheduler.start();
            scheduledJobs.put(entity.getId(), job);
        } catch (SchedulerException e) {
            if (!isJobExpired(e.getMessage())) {
                LOGGER.error("schedule backup job exception: {}", ExceptionUtils.getStackTrace(e));
                throw new BatException(ErrorCode.CREATE_JOB_ERROR);
            }
            LOGGER.warn("Schedule job {} skipped because its plan time is past", entity);
        }
    }

    /**
     * 备份集应用恢复，恢复之前先暂停待恢复实例的所有备份任务，待恢复完成后再启动待恢复实例的备份任务
     * @param taskEntity 需要恢复的备份集
     */
    public void restoreBackup(TaskEntity taskEntity) throws BatException {
        long instanceId = taskEntity.getInstanceId();
        List<BackupJob> instanceBackupJobs = scheduledJobs.values().stream()
            .filter(backupJob -> backupJob.getInstanceId() == instanceId).collect(Collectors.toList());
        pauseBackupJobs(instanceBackupJobs);
        try {
            JobDetail jobDetail = JobBuilder.newJob(RestoreExecutor.class)
                .withIdentity("restore-" + taskEntity.getId()).build();
            jobDetail.getJobDataMap().put(Const.JOB_KEY, taskEntity);
            SimpleTrigger jobTrigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                .withIdentity("restore-" + taskEntity.getId())
                .startNow().build();

            scheduler.scheduleJob(jobDetail, jobTrigger);
            scheduler.start();
        } catch (SchedulerException e) {
            LOGGER.error("schedule restore job exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BatException(ErrorCode.BACKUP_RESTORE_ERROR);
        }

        resumeBackupJobs(instanceBackupJobs);
    }

    /**
     * 删除定时任务
     */
    public void deleteBackupJobs(List<JobEntity> jobs) {
        List<JobKey> jobKeys = jobs.stream().map(job -> JobKey.jobKey(scheduledJobs.get(job.getId()).getName()))
            .collect(Collectors.toList());
        try {
            scheduler.deleteJobs(jobKeys);
        } catch (SchedulerException e) {
            LOGGER.error("delete schedule job {} exception: {}", jobs, ExceptionUtils.getStackTrace(e));
            throw new BatException(ErrorCode.REMOVE_JOB_ERROR);
        }

        deleteJobBackupPath(jobs);
        jobs.forEach(job -> scheduledJobs.remove(job.getId()));
    }

    private void pauseBackupJobs(List<BackupJob> backupJobs) throws BatException {
        for (BackupJob backupJob : backupJobs) {
            try {
                scheduler.pauseJob(JobKey.jobKey(backupJob.getName()));
            } catch (SchedulerException e) {
                LOGGER.error("pause job [{}] failed : {}", backupJob.getName(), ExceptionUtils.getStackTrace(e));
                throw new BatException(ErrorCode.NO_FULL_BACKUP_ERROR);
            }
        }
    }

    private void resumeBackupJobs(List<BackupJob> backupJobs) throws BatException {
        for (BackupJob backupJob : backupJobs) {
            try {
                scheduler.resumeJob(JobKey.jobKey(backupJob.getName()));
            } catch (SchedulerException e) {
                LOGGER.error("resume job [{}] failed : {}", backupJob.getName(), ExceptionUtils.getStackTrace(e));
                throw new BatException(ErrorCode.RESUME_JOB_ERROR);
            }
        }
    }

    private void deleteJobBackupPath(List<JobEntity> jobs) {
        for (JobEntity job : jobs) {
            InstanceEntity instance = instanceService.getInstanceById(job.getInstanceId());
            String password = instanceService.getPlainPassword(instance.getOsPassword(), instance.getId());

            Session session = SSHUtils.getSession(instance.getInstanceIp(), instance.getInstancePort(),
                instance.getOsUser(), password);
            String jobPath = PathUtils.buildJobPath(instance.getBackupPath(), job.getId());
            StringBuilder cmdBuilder = new StringBuilder();
            cmdBuilder.append("rm -rf ").append(jobPath);
            SSHUtils.execRemoteCmd(session, cmdBuilder.toString(), "");

            cmdBuilder.setLength(0);
            cmdBuilder.append("ls -al ").append(jobPath);
            String cmdResult = SSHUtils.execRemoteCmd(session, cmdBuilder.toString(), "");
            if (!StringUtils.contains(cmdResult, "No such file or directory")) {
                LOGGER.error("delete backup data path for job [{}] failed", job.getId());
                throw new BatException(ErrorCode.DELETE_JOB_PATH_ERROR);
            }

            session.disconnect();
        }
    }

    private void scheduleBackupJobsIfNeed(JobEntity entity) {
        try {
            BackupJob job = buildBackupJob(entity);
            if (!scheduler.checkExists(TriggerKey.triggerKey(job.getName()))) {
                scheduler.scheduleJob(job.getJobDetail(), job.getJobTrigger());
                scheduler.start();
            } else {
                LOGGER.info("Job with id [{}] already scheduled", entity.getId());
            }
            scheduledJobs.put(entity.getId(), job);
        } catch (SchedulerException e) {
            LOGGER.error("check entity [{}] cron trigger exception:{}", entity.getId(), ExceptionUtils.getStackTrace(e));
            throw new BatException(ErrorCode.CHECK_JOB_TRIGGER_ERROR);
        }
    }

    private boolean isJobExpired(String errorMsg) {
        return StringUtils.contains(errorMsg, "Based on configured schedule") &&
            StringUtils.contains(errorMsg, "will never fire");
    }

    private synchronized BackupJob buildBackupJob(JobEntity entity) {
        if (StringUtils.equals(entity.getType(), Const.LOGICAL_BACKUP)) {
            return new LogicalBackup(entity);
        } else if (StringUtils.equals(entity.getType(), Const.PHYSICAL_BACKUP)) {
            return new PhysicalBackup(entity);
        } else {
            LOGGER.error("unrecognized job type : {}", entity.getType());
            throw new BatException(ErrorCode.UNRECOGNIZED_OPERATION);
        }
    }

    private void modifyGUCParameterIfNeed(InstanceEntity instance, JobEntity job) {
        if (StringUtils.equals(job.getOption(), Const.FULL_BACKUP)) {
            LOGGER.info("job {} method is full backup, no need to modify guc parameter", job.getId());
            return;
        }

        String cmd = "gs_guc reload -N all -I all -c \"enable_cbm_tracking=on\"";
        Session session = SSHUtils.getSession(instance.getInstanceIp(), instance.getInstancePort(),
            instance.getOsUser(), instance.getOsPassword());
        try {
            String cmdResult = SSHUtils.execRemoteCmd(session, cmd, "");
            if (StringUtils.isBlank(cmdResult)) {
                LOGGER.error("create file storage path for job [{}] failed", job.getId());
                throw new BatException(ErrorCode.JOB_PATH_ERROR);
            }
        } catch (BatException batException) {
            throw batException;
        } finally {
            session.disconnect();
        }
    }

    private void makeJobPathIfAbsent(InstanceEntity instance, JobEntity job) {
        if (StringUtils.equals(job.getOption(), Const.INCREMENT_BACKUP)) {
            LOGGER.info("job {} method is increment backup, no need to make job path", job.getId());
            return;
        }
        Session session = SSHUtils.getSession(instance.getInstanceIp(), instance.getInstancePort(),
            instance.getOsUser(), instance.getOsPassword());
        String jobPath = PathUtils.buildJobPath(instance.getBackupPath(), job.getId());
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("if [ ! -d ").append(jobPath)
            .append(" ]; then mkdir -p ").append(jobPath)
            .append("; fi && ls -al ").append(jobPath);
        try {
            String cmdResult = SSHUtils.execRemoteCmd(session, cmdBuilder.toString(), "");
            if (StringUtils.isBlank(cmdResult)) {
                LOGGER.error("create file storage path for job [{}] failed", job.getId());
                throw new BatException(ErrorCode.JOB_PATH_ERROR);
            }

            if (StringUtils.equalsIgnoreCase(job.getType(), Const.PHYSICAL_BACKUP) &&
                StringUtils.equalsIgnoreCase(job.getOption(), Const.FULL_BACKUP)) {
                LOGGER.info("physical full backup, need to init the backup path");
                initPhysicalBackupPath(jobPath, session);
            }
        } catch (BatException batException) {
            throw batException;
        } finally {
            session.disconnect();
        }
    }

    private void initPhysicalBackupPath(String jobPath, Session session) {
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append(Const.PHYSICAL_BACKUP_CMD).append(" init  -B ").append(jobPath);
        String result = SSHUtils.execRemoteCmd(session, cmdBuilder.toString(), "");
        if (!StringUtils.contains(result, "successfully inited")) {
            LOGGER.error("{}", result);
            throw new BatException(ErrorCode.INIT_BACKUP_PATH_ERROR);
        }
        LOGGER.info("{}", result);
    }
}
