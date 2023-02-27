/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.batman.modules.dao.SysUserDao;
import org.opengauss.batman.modules.dao.SysUserTokenDao;
import org.opengauss.batman.modules.entity.MenuEntity;
import org.opengauss.batman.modules.entity.UserEntity;
import org.opengauss.batman.modules.entity.UserTokenEntity;
import org.opengauss.batman.modules.service.ShiroService;
import org.opengauss.batman.modules.dao.SysMenuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShiroServiceImpl implements ShiroService {
    @Autowired
    private SysMenuDao sysMenuDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserTokenDao sysUserTokenDao;

    @Override
    public Set<String> getUserPermissions(long userId) {
        List<String> permsList;

        //系统管理员，拥有最高权限
        List<MenuEntity> menuList = sysMenuDao.selectList(null);
        permsList = new ArrayList<>(menuList.size());
        for(MenuEntity menu : menuList){
            permsList.add(menu.getPerms());
        }
        //用户权限列表
        Set<String> permsSet = new HashSet<>();
        for(String perms : permsList){
            if(StringUtils.isBlank(perms)){
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        return permsSet;
    }

    @Override
    public UserTokenEntity queryByToken(String token) {
        return sysUserTokenDao.queryByToken(token);
    }

    @Override
    public UserEntity queryUser(Long userId) {
        return sysUserDao.selectById(userId);
    }
}
