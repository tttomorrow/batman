package org.opengauss.batman.modules.execution;

import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
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

import java.io.File;
import java.io.Serializable;

public class LogicalBackup extends BackupJob implements Serializable  {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogicalBackup.class);

    public LogicalBackup(JobEntity entity) {
        super(entity);
    }

    @Override
    public void execute(JobService jobService, TaskService taskService, InstanceEntity instance, TaskEntity task) {
        Session session = SSHUtils.getSession(instance.getInstanceIp(), instance.getInstancePort(),
            instance.getOsUser(), instance.getOsPassword());
        String jobPath = PathUtils.buildJobPath(instance.getBackupPath(), jobEntity.getId());
        String outPath = jobPath + File.separator + task.getId() + ".sql";

        StringBuilder cmder = new StringBuilder();
        cmder.append("gs_dumpall -f ").append(outPath).append(" -p ").append(instance.getDbPort());

        SSHUtils.execRemoteCmd(session, cmder.toString(), "");

        cmder.setLength(0);
        cmder.append("du -sk ").append(outPath).append(" | awk '{print $1}'");
        String cmdResult = SSHUtils.execRemoteCmd(session, cmder.toString(), "");
        if (StringUtils.isBlank(cmdResult)) {
            LOGGER.error("get backup result size failed");
            session.disconnect();
            throw new BatException(ErrorCode.BACKUP_PATH_ERROR);
        }
        session.disconnect();
        if (!StringUtils.isNumeric(cmdResult)) {
            LOGGER.error("logical backup failed, can not parse backup data size: {}", cmdResult);
            throw new BatException(ErrorCode.LOGICAL_BACKUP_ERROR);
        }
        task.setSize(cmdResult + "KB");
    }
}
