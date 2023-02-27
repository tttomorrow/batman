package org.opengauss.batman.modules.controller;

import org.opengauss.batman.common.annotation.SysLog;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.R;
import org.opengauss.batman.modules.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sys/task")
public class TaskController extends AbstractController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = taskService.queryPage(params);
        return R.ok().put("page", page);
    }

    @SysLog("删除备份任务")
    @DeleteMapping("/delete")
    public R delete(@RequestBody Long[] taskIds){
        try {
            taskService.deleteBatch(taskIds);
            return R.ok();
        } catch (BatException e) {
            return R.error(e.getCode(), e.getMessage());
        }
    }

    @SysLog("应用备份数据")
    @PutMapping("/restore/{taskId}")
    public R restore(@PathVariable("taskId") Long taskId){
        try {
            taskService.restore(taskId);
            return R.ok();
        } catch (BatException e) {
            return R.error(e.getCode(), e.getMessage());
        }
    }
}
