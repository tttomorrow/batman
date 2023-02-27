package org.opengauss.batman.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.modules.entity.TaskEntity;

import java.util.Map;

public interface TaskService extends IService<TaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void createTask(TaskEntity taskEntity);

    void deleteBatch(Long[] taskIds);

    boolean restore(Long taskId);

    void updateTask(TaskEntity task);

    TaskEntity queryLatestTaskByJobId(long jobId);
}
