/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.modules.entity.UserEntity;

import java.util.Map;


/**
 * 系统用户
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface UserService extends IService<UserEntity> {

	PageUtils queryPage(Map<String, Object> params);

	/**
	 * 根据用户名，查询系统用户
	 */
	UserEntity queryByUserName(String username);

	/**
	 * 保存用户
	 */
	void saveUser(UserEntity user);
	
	/**
	 * 修改用户
	 */
	void update(UserEntity user);
	
	/**
	 * 删除用户
	 */
	void deleteBatch(Long[] userIds);

	/**
	 * 修改密码
	 * @param userId       用户ID
	 * @param password     原密码
	 * @param newPassword  新密码
	 */
	boolean updatePassword(Long userId, String password, String newPassword);
}
