package org.opengauss.batman.modules.execution;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.opengauss.batman.common.Const;
import org.opengauss.batman.common.annotation.SysLog;
import org.opengauss.batman.modules.entity.InstanceEntity;
import org.opengauss.batman.modules.entity.TaskEntity;
import org.opengauss.batman.modules.service.InstanceService;
import org.opengauss.batman.modules.service.JobService;
import org.opengauss.batman.modules.service.TaskService;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * 备份计划执行模块
 * 当前先采用SshUtil远程调用gs_dump和gs_probackup的方式进行备份
 * 后续需要改造为微服务调用的方式，即增加Agent模块，通过GRPC的方式执行
 *
 */
@Component
public class BackupExecutor extends QuartzJobBean {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JobService jobService;

    @Autowired
    private InstanceService instanceService;

	private static final Logger LOGGER = LoggerFactory.getLogger(BackupExecutor.class);

    @Override
    @SysLog("执行备份任务")
    protected void executeInternal(JobExecutionContext context) {
        BackupJob backupJob = (BackupJob) context.getMergedJobDataMap().get(Const.JOB_KEY);
        //任务开始时间
        TaskEntity task = backupJob.buildTaskEntry();
        try {
            task.setStatus(Const.TASK_RUNNING);
            taskService.createTask(task);

            // 开始执行任务
            LOGGER.info("start to execute backup task [{}] for instance [{}]", task.getName(), task.getInstanceId());
            InstanceEntity instance = instanceService.getInstanceById(backupJob.getInstanceId());
            String password = instanceService.getPlainPassword(instance.getOsPassword(), instance.getId());
            instance.setOsPassword(password);

            backupJob.execute(jobService, taskService, instance, task);
            //任务执行结束
            LOGGER.info("end to execute backup task [{}] for instance [{}]", task.getName(), task.getInstanceId());
            task.setStatus(Const.TASK_SUCCESS);
		} catch (Exception e) {
            LOGGER.error("execute backup task [{}] for instance [{}] exception：{}",
                task.getName(), task.getInstanceId(), ExceptionUtils.getStackTrace(e));
            task.setStatus(Const.TASK_FAILED);
		} finally {
            task.setEndTime(new Date());
            taskService.updateTask(task);
        }
    }
}
