package org.opengauss.batman.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SERVER_CONNECT_ERROR(5001, "can not connect to the remote server"),

    INSTANCE_VERSION_ERROR(5002, "can not parse instance version from command line"),

    BACKUP_PATH_ERROR(5003, "backup path not has the permission"),

    CREATE_JOB_ERROR(5004, "create new schedule job failed"),

    REMOVE_JOB_ERROR(5005, "remove schedule job failed"),

    JOB_PATH_ERROR(5006, "create backup job file storage path failed"),

    CREATE_TASK_ERROR(5007, "schedule new task failed"),

    LOGICAL_BACKUP_ERROR(5007, "logical backup failed"),

    CHECK_JOB_TRIGGER_ERROR(5008, "check job cron trigger failed"),

    UNRECOGNIZED_OPERATION(5009, "unrecognized operation"),

    PARSE_INSTANCE_INFO_ERROR(5010, "parse instance node info error"),

    INIT_BACKUP_PATH_ERROR(5010, "init the backup path for physical backup failed"),

    BACKUP_ADD_INSTANCE_ERROR(5011, "gs_probackup add instance error"),

    BACKUP_DO_WORK_ERROR(5012, "gs_probackup do the backup error"),

    PARSE_BACKUP_ID_ERROR(5013, "parse backup id error"),

    PARSE_BACKUP_SIZE_ERROR(5014, "parse backup data size error"),

    DELETE_JOB_PATH_ERROR(5015, "delete job backup data path error"),

    BACKUP_SIZE_ERROR(5016, "get backup file size error"),

    SHOW_BACKUP_INSTANCE_ERROR(5017, "show backup instance info error"),

    NO_FULL_BACKUP_ERROR(5018, "full backup not exist"),

    DUPLICATE_JOB_ERROR(5019, "duplicate job exist"),

    PAUSE_JOB_ERROR(5020, "pause backup job failed"),

    RESUME_JOB_ERROR(5021, "resume backup job failed"),

    BACKUP_RESTORE_ERROR(5022, "backup data restore failed");

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    //错误码
    private final int code;

    //错误消息
    private final String message;
}
