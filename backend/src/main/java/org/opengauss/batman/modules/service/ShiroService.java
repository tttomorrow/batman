/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.service;

import org.opengauss.batman.modules.entity.UserEntity;
import org.opengauss.batman.modules.entity.UserTokenEntity;

import java.util.Set;

/**
 * shiro相关接口
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface ShiroService {
    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(long userId);

    UserTokenEntity queryByToken(String token);

    /**
     * 根据用户ID，查询用户
     * @param userId
     */
    UserEntity queryUser(Long userId);
}
