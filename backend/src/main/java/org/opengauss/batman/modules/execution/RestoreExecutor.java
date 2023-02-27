package org.opengauss.batman.modules.execution;

import com.jcraft.jsch.Session;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.batman.common.Const;
import org.opengauss.batman.common.ErrorCode;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.SSHUtils;
import org.opengauss.batman.modules.entity.InstanceEntity;
import org.opengauss.batman.modules.entity.TaskEntity;
import org.opengauss.batman.modules.service.InstanceService;
import org.opengauss.batman.modules.service.TaskService;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RestoreExecutor extends QuartzJobBean {

    @Autowired
    private TaskService taskService;

    @Autowired
    private InstanceService instanceService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupExecutor.class);

    private static final String STOP_SERVER_CMD = "gs_om -t stop -m fast";

    private static final String START_SERVER_CMD = "gs_om -t start";

    private static final Pattern RESTORE_COMPLETE = Pattern.compile("Restore of backup\\s*\\w+\\s*completed");

    /**
     * 备份恢复执行流程：
     *  1）关闭数据库实例
     *  2）删除当前数据库数据路径
     *  3）执行gs_probackup restore恢复
     *  4）启动数据库实例
     * @param context 执行上下文
     */
    @Override
    protected void executeInternal(JobExecutionContext context) {
        TaskEntity taskEntity = (TaskEntity) context.getMergedJobDataMap().get(Const.JOB_KEY);
        TaskEntity restoreTask = buildRestoreTaskEntity(taskEntity);
        Session remoteSession = null;
        try {
            restoreTask.setStatus(Const.TASK_RUNNING);
            taskService.createTask(restoreTask);

            LOGGER.info("start to execute restore task [{}] for instance [{}]",
                restoreTask.getName(), restoreTask.getInstanceId());
            InstanceEntity instance = instanceService.getInstanceById(restoreTask.getInstanceId());
            String password = instanceService.getPlainPassword(instance.getOsPassword(), instance.getId());
            instance.setOsPassword(password);
            remoteSession = SSHUtils.getSession(instance.getInstanceIp(), instance.getInstancePort(),
                instance.getOsUser(), instance.getOsPassword());

            // 停止数据库实例
            String cmdResult = SSHUtils.execRemoteCmd(remoteSession, STOP_SERVER_CMD, "");
            if (!StringUtils.contains(cmdResult, "Successfully stopped cluster")) {
                LOGGER.error("stop cluster failed, error msg: {}", cmdResult);
                throw new BatException(ErrorCode.BACKUP_RESTORE_ERROR);
            }

            // 删除数据库数据目录
            String removeCmd = "rm -rf " + instance.getDbDataPath();
            SSHUtils.execRemoteCmd(remoteSession, removeCmd, "");

            // 恢复到指定备份集
            StringBuilder restoreCmdBuilder = new StringBuilder();
            restoreCmdBuilder.append(Const.PHYSICAL_BACKUP_CMD)
                .append(" restore -B ").append(restoreTask.getBackupDataPath())
                .append(" --instance=").append(restoreTask.getBackupInstanceName())
                .append(" -D ").append(instance.getDbDataPath())
                .append(" -i ").append(restoreTask.getBackupId());
            cmdResult = SSHUtils.execRemoteCmd(remoteSession, restoreCmdBuilder.toString(), "");
            Matcher resultMatcher = RESTORE_COMPLETE.matcher(cmdResult);
            if (!resultMatcher.find()) {
                LOGGER.error("restore backup for instance failed: {}", cmdResult);
                throw new BatException(ErrorCode.BACKUP_RESTORE_ERROR);
            }

            // 启动数据库实例
            cmdResult = SSHUtils.execRemoteCmd(remoteSession, START_SERVER_CMD, "");
            if (!StringUtils.contains(cmdResult, "Successfully started")) {
                LOGGER.error("start cluster failed, error msg: {}", cmdResult);
                throw new BatException(ErrorCode.RESUME_JOB_ERROR);
            }

            // 任务执行结束
            LOGGER.info("end to execute restore task [{}] for instance [{}]",
                restoreTask.getName(), restoreTask.getInstanceId());
            restoreTask.setStatus(Const.TASK_SUCCESS);
        } catch (Exception e) {
            LOGGER.error("execute restore task [{}] for instance [{}] exception：{}",
                restoreTask.getName(), restoreTask.getInstanceId(), ExceptionUtils.getStackTrace(e));
            restoreTask.setStatus(Const.TASK_FAILED);
        } finally {
            restoreTask.setEndTime(new Date());
            taskService.updateTask(restoreTask);
            if (remoteSession != null) {
                remoteSession.disconnect();
            }
        }
    }

    private TaskEntity buildRestoreTaskEntity(TaskEntity entity) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setJobId(entity.getJobId());
        taskEntity.setType(Const.TASK_RESTORE);
        taskEntity.setStartTime(new Date());
        taskEntity.setInstanceId(entity.getInstanceId());
        taskEntity.setName(buildTaskName(entity));
        taskEntity.setBackupDataPath(entity.getBackupDataPath());
        taskEntity.setBackupInstanceName(entity.getBackupInstanceName());
        taskEntity.setBackupId(entity.getBackupId());
        return taskEntity;
    }

    private String buildTaskName(TaskEntity task) {
        StringBuilder sb = new StringBuilder();
        sb.append("restore-").append(task.getInstanceId()).append("-")
            .append(task.getId()).append("-").append(System.currentTimeMillis() / Const.MILLIS_PER_SECOND);
        return sb.toString();
    }
}
