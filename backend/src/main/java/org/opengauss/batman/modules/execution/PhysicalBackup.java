package org.opengauss.batman.modules.execution;

import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.batman.common.Const;
import org.opengauss.batman.common.ErrorCode;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.SSHUtils;
import org.opengauss.batman.modules.entity.InstanceEntity;
import org.opengauss.batman.modules.entity.JobEntity;
import org.opengauss.batman.modules.entity.TaskEntity;
import org.opengauss.batman.modules.service.JobService;
import org.opengauss.batman.modules.service.TaskService;
import org.opengauss.batman.modules.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PhysicalBackup extends BackupJob implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalBackup.class);

    private static final Pattern BACKUP_ID_PATTERN = Pattern.compile("backup ID:\\s*(\\w+)");

    private static final Pattern BACKUP_SIZE_PATTERN = Pattern.compile("Backup\\s*\\w+\\s*resident size:\\s*(\\w+)");

    public PhysicalBackup(JobEntity entity) {
        super(entity);
    }

    @Override
    public void execute(JobService jobService, TaskService taskService, InstanceEntity instance, TaskEntity task) {
        Session session = SSHUtils.getSession(instance.getInstanceIp(), instance.getInstancePort(),
            instance.getOsUser(), instance.getOsPassword());
        try {
            String jobPath;
            String backupInstanceName;
            if (StringUtils.equals(jobEntity.getOption(), Const.FULL_BACKUP)) {
                jobPath = PathUtils.buildJobPath(instance.getBackupPath(), jobEntity.getId());
                backupInstanceName = jobEntity.getId() + "_" + task.getId();
            } else {
                long fullBackupJobId = jobService.queryPhysicalJobByInstanceId(instance.getId()).stream()
                    .filter(job -> StringUtils.equals(job.getOption(), Const.FULL_BACKUP))
                    .map(job -> job.getId()).collect(Collectors.toList()).get(0);
                TaskEntity fullBackupTask = taskService.queryLatestTaskByJobId(fullBackupJobId);
                jobPath = PathUtils.buildJobPath(instance.getBackupPath(), fullBackupTask.getJobId());
                backupInstanceName = fullBackupTask.getJobId() + "_" + fullBackupTask.getId();
            }

            task.setBackupDataPath(jobPath);
            task.setBackupInstanceName(backupInstanceName);

            if (StringUtils.equals(jobEntity.getOption(), Const.FULL_BACKUP)) {
                StringBuilder cmder = new StringBuilder();
                cmder.append(Const.PHYSICAL_BACKUP_CMD).append(" add-instance -B ").append(jobPath).append(" -D ")
                    .append(instance.getDbDataPath()).append(" --instance ").append(backupInstanceName);

                String result = SSHUtils.execRemoteCmd(session, cmder.toString(), "");
                if (!StringUtils.contains(result, "successfully inited")) {
                    LOGGER.error("gs_probackup add-instance failed: {}", result);
                    throw new BatException(ErrorCode.BACKUP_ADD_INSTANCE_ERROR);
                }
            }

            StringBuilder cmdBuilder = new StringBuilder();
            cmdBuilder.append(Const.PHYSICAL_BACKUP_CMD).append(" backup -B ").append(jobPath)
                .append(" --instance ").append(backupInstanceName)
                .append(" -d postgres -p ").append(instance.getDbPort()).append(" -b ")
                .append(StringUtils.equals(jobEntity.getOption(), Const.FULL_BACKUP)? "FULL" : "PTRACK");

            String cmdResult = SSHUtils.execRemoteCmd(session, cmdBuilder.toString(), "\n");
            if (StringUtils.isBlank(cmdResult)) {
                LOGGER.error("gs_probackup backup work failed");
                throw new BatException(ErrorCode.BACKUP_DO_WORK_ERROR);
            }
            task.setBackupId(parseBackupId(cmdResult));
            task.setSize(parseBackupDataSize(cmdResult));
        } catch (BatException batException) {
            throw batException;
        } finally {
            LOGGER.info("close the remote session");
            session.disconnect();
        }
    }

    private String parseBackupDataSize(String input) {
        Matcher matcher = BACKUP_SIZE_PATTERN.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        LOGGER.error("parse backup data size error: {}", input);
        throw new BatException(ErrorCode.PARSE_BACKUP_SIZE_ERROR);
    }

    private String parseBackupId(String input) {
        Matcher matcher = BACKUP_ID_PATTERN.matcher(input);
        if (matcher.find()) {
           return matcher.group(1);
        }

        LOGGER.error("can not parse backup id from input: {}", input);
        throw new BatException(ErrorCode.PARSE_BACKUP_ID_ERROR);
    }
}
