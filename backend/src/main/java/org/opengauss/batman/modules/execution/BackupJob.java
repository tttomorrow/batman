package org.opengauss.batman.modules.execution;

import org.apache.commons.lang.StringUtils;
import org.opengauss.batman.common.Const;
import org.opengauss.batman.modules.entity.InstanceEntity;
import org.opengauss.batman.modules.entity.JobEntity;
import org.opengauss.batman.modules.entity.TaskEntity;
import org.opengauss.batman.modules.service.JobService;
import org.opengauss.batman.modules.service.TaskService;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class BackupJob implements Serializable {

    private static final String BACKUP_TASK = "backup_task";

    protected JobEntity jobEntity;

    private JobDetail jobDetail;

    private Trigger jobTrigger;

    public BackupJob(JobEntity entity) {
        this.jobEntity = entity;
        initBackupJobScheduleInfo();
    }

    private void initBackupJobScheduleInfo() {
        jobDetail = JobBuilder.newJob(BackupExecutor.class).withIdentity(getName()).build();
        jobTrigger = TriggerBuilder.newTrigger().withIdentity(getName()).withSchedule(buildScheduleBuilder())
            .startAt(jobEntity.getStartTime()).build();
        jobDetail.getJobDataMap().put(Const.JOB_KEY, this);
    }

    public String getName() {
        return BACKUP_TASK + jobEntity.getId();
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public Trigger getJobTrigger() {
        return jobTrigger;
    }

    public long getInstanceId() {
        return jobEntity.getInstanceId();
    }

    public TaskEntity buildTaskEntry() {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setName(buildTaskName(jobEntity));
        taskEntity.setInstanceId(jobEntity.getInstanceId());
        taskEntity.setJobId(jobEntity.getId());
        taskEntity.setType(jobEntity.getType());
        taskEntity.setStartTime(new Date());
        return taskEntity;
    }

    public void execute(JobService jobService, TaskService taskService, InstanceEntity instance, TaskEntity task) {

    }

    private ScheduleBuilder buildScheduleBuilder() {
        if (StringUtils.equals(jobEntity.getOption(), Const.FULL_BACKUP)) {
            return CronScheduleBuilder.cronSchedule(buildFullBackupCron(jobEntity))
                .withMisfireHandlingInstructionDoNothing();
        } else {
            return SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(Integer.parseInt(jobEntity.getSchedule()))
                .withMisfireHandlingInstructionIgnoreMisfires().repeatForever();
        }
    }

    private String buildFullBackupCron(JobEntity job) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(job.getStartTime());
        StringBuilder cronBuilder = new StringBuilder();
        cronBuilder.append(calendar.get(Calendar.SECOND)).append(" ")
            .append(calendar.get(Calendar.MINUTE)).append(" ")
            .append(calendar.get(Calendar.HOUR_OF_DAY)).append(" ");

        if (StringUtils.isNotEmpty(job.getSchedule())) {
            cronBuilder.append("? * ").append(job.getSchedule());
            return cronBuilder.toString();
        }

        cronBuilder.append(calendar.get(Calendar.DAY_OF_MONTH)).append(" ")
            .append(calendar.get(Calendar.MONTH) + 1).append(" ? ")
            .append(calendar.get(Calendar.YEAR));

        return cronBuilder.toString();
    }

    /**
     * 构建此次备份任务的任务名称，采用备份计划名称@当前UTC秒数的格式
     * @param job 需要执行的备份计划
     * @return 构建的备份任务名称
     */
    private String buildTaskName(JobEntity job) {
        StringBuilder sb = new StringBuilder();
        sb.append("backup-").append(job.getInstanceId()).append("-")
            .append(job.getId()).append("-").append(System.currentTimeMillis() / Const.MILLIS_PER_SECOND);
        return sb.toString();
    }
}
