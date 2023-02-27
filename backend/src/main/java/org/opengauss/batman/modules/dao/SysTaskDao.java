package org.opengauss.batman.modules.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.batman.modules.entity.TaskEntity;

@Mapper
public interface SysTaskDao extends BaseMapper<TaskEntity>  {
    void insertTask(TaskEntity taskEntity);

    /**
     * 查询所有的任务
     */
    void updateTaskById(TaskEntity task);

    long queryTaskIdByName(String taskName);

    TaskEntity queryLatestTaskByJobId(long jobId);

    TaskEntity queryTaskById(long taskId);
}