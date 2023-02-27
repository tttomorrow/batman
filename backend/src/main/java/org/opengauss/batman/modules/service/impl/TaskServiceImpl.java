package org.opengauss.batman.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opengauss.batman.common.ErrorCode;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.Query;
import org.opengauss.batman.modules.dao.SysTaskDao;
import org.opengauss.batman.modules.entity.TaskEntity;
import org.opengauss.batman.modules.execution.BackupManager;
import org.opengauss.batman.modules.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

@Service("sysTaskService")
public class TaskServiceImpl extends ServiceImpl<SysTaskDao, TaskEntity> implements TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private BackupManager backupManager;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<TaskEntity> page = this.page(new Query<TaskEntity>().getPage(params), new QueryWrapper<>());
        return new PageUtils(page);
    }

    @Override
    public void createTask(TaskEntity taskEntity) {
        try {
            baseMapper.insertTask(taskEntity);
            taskEntity.setId(baseMapper.queryTaskIdByName(taskEntity.getName()));
        } catch (Exception e) {
            LOGGER.error("insert task record exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BatException(ErrorCode.CREATE_TASK_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] taskIds) {
        this.removeByIds(Arrays.asList(taskIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restore(Long taskId) {
        TaskEntity taskEntity = baseMapper.queryTaskById(taskId);
        backupManager.restoreBackup(taskEntity);
        return true;
    }

    @Override
    public void updateTask(TaskEntity task) {
        baseMapper.updateTaskById(task);
    }

    @Override
    public TaskEntity queryLatestTaskByJobId(long jobId) {
        return baseMapper.queryLatestTaskByJobId(jobId);
    }
}
