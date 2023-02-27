package org.opengauss.batman.modules.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_task")
public class TaskEntity implements Serializable {

    @TableId
    private Long id;

    @Positive(message = "备份实例ID不能为空")
    private Long instanceId;

    private Long jobId;

    @NotBlank(message="任务名称不能为空")
    private String name;

    private String type;

    private Date startTime;

    private Date endTime;

    private Integer progress;

    private String status;

    private String size;

    private String backupDataPath;

    private String backupInstanceName;

    private String backupId;
}
