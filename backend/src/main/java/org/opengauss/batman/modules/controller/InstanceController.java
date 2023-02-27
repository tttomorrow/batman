package org.opengauss.batman.modules.controller;


import org.apache.commons.lang.exception.ExceptionUtils;
import org.opengauss.batman.common.annotation.SysLog;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.R;
import org.opengauss.batman.common.validator.ValidatorUtils;
import org.opengauss.batman.modules.entity.InstanceEntity;
import org.opengauss.batman.modules.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/sys/instance")
public class InstanceController extends AbstractController {
	@Autowired
	private InstanceService instanceService;
	
	/**
	 * 返回系统中所有的实例列表
	 */
	@GetMapping("/list")
	public R list(@RequestParam Map<String, Object> params){
		PageUtils page = instanceService.queryPage(params);

		return R.ok().put("page", page);
	}

	@GetMapping("/info/{instanceId}")
	public R info(@PathVariable("instanceId") Long instanceId){
		InstanceEntity instance = instanceService.getById(instanceId);
		return R.ok().put("data", instance);
	}

	/**
	 * 保存配置
	 */
	@SysLog("新增备份实例")
	@PostMapping("/new")
	public R save(@RequestBody InstanceEntity instance){
		ValidatorUtils.validateEntity(instance);
		try {
			Map<String, Object> result = instanceService.newInstance(instance);
			return R.ok(result);
		} catch (BatException e) {
			return R.error(e.getCode(), e.getMsg());
		}
	}
	
	/**
	 * 修改配置
	 */
	@SysLog("修改备份实例")
	@PutMapping("/update")
	public R update(@RequestBody InstanceEntity instance){
		ValidatorUtils.validateEntity(instance);
		try {
			instanceService.updateInstance(instance);
			return R.ok();
		} catch (BatException e) {
			logger.error("update instance info exception: {}", ExceptionUtils.getStackTrace(e));
			return R.error(e.getCode(), e.getMsg());
		}
	}
	
	/**
	 * 删除配置
	 */
	@SysLog("删除备份实例")
	@DeleteMapping("/delete")
	public R delete(@RequestBody Long[] ids){
		instanceService.deleteBatch(ids);
		return R.ok();
	}
}
