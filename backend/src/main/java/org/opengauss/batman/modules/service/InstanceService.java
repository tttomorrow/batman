package org.opengauss.batman.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.modules.entity.InstanceEntity;

import java.util.Map;

/**
 * 系统配置信息
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface InstanceService extends IService<InstanceEntity> {

	PageUtils queryPage(Map<String, Object> params);
	
	/**
	 * 保存配置信息
	 */
	Map<String, Object> newInstance(InstanceEntity config);
	
	/**
	 * 更新配置信息

	 */
	void updateInstance(InstanceEntity config);

	/**
	 * 删除配置信息
	 */
	void deleteBatch(Long[] ids);

	InstanceEntity getInstanceById(long instanceId);

	String getPlainPassword(String password, long instanceId);
}
