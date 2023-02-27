/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.batman.modules.entity.UserEntity;

/**
 * 系统用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface SysUserDao extends BaseMapper<UserEntity> {

	/**
	 * 根据用户名，查询系统用户
	 */
	UserEntity queryByUserName(String username);

}
