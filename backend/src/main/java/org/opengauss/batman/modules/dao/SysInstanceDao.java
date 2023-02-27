package org.opengauss.batman.modules.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.batman.modules.entity.InstanceEntity;

/**
 * 系统配置信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface SysInstanceDao extends BaseMapper<InstanceEntity> {

	InstanceEntity queryInstanceByName(String instanceName);

	InstanceEntity queryInstanceById(Long instanceId);

	Long insertNewInstance(InstanceEntity entity);
}
