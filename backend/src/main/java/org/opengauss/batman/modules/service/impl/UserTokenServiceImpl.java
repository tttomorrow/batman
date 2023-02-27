/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.batman.common.utils.R;
import org.opengauss.batman.modules.dao.SysUserTokenDao;
import org.opengauss.batman.modules.entity.UserTokenEntity;
import org.opengauss.batman.modules.oauth2.TokenGenerator;
import org.opengauss.batman.modules.service.UserTokenService;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service("sysUserTokenService")
public class UserTokenServiceImpl extends ServiceImpl<SysUserTokenDao, UserTokenEntity> implements UserTokenService {
	//1小时后过期
	private final static int EXPIRE = 3600;

	@Override
	public R createToken(long userId) {
		//生成一个token
		String token = TokenGenerator.generateValue();

		//当前时间
		Date now = new Date();
		//过期时间
		Date expireTime = new Date(now.getTime() + EXPIRE * 1000);

		//判断是否生成过token
		UserTokenEntity tokenEntity = this.getById(userId);
		if (tokenEntity == null){
			tokenEntity = new UserTokenEntity();
			tokenEntity.setUserId(userId);
			tokenEntity.setToken(token);
			tokenEntity.setUpdateTime(now);
			tokenEntity.setExpireTime(expireTime);
			//保存token
			this.save(tokenEntity);
		} else {
			tokenEntity.setToken(token);
			tokenEntity.setUpdateTime(now);
			tokenEntity.setExpireTime(expireTime);
			//更新token
			this.updateById(tokenEntity);
		}

		R r = R.ok().put("token", token).put("expire", EXPIRE);
		return r;
	}

	@Override
	public void logout(long userId) {
		//生成一个token
		String token = TokenGenerator.generateValue();

		//修改token
		UserTokenEntity tokenEntity = new UserTokenEntity();
		tokenEntity.setUserId(userId);
		tokenEntity.setToken(token);
		this.updateById(tokenEntity);
	}
}
