/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.controller;

import org.apache.shiro.crypto.hash.Sha256Hash;
import org.opengauss.batman.common.Const;
import org.opengauss.batman.common.annotation.SysLog;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.R;
import org.opengauss.batman.common.validator.Assert;
import org.opengauss.batman.modules.entity.UserEntity;
import org.opengauss.batman.modules.form.PasswordForm;
import org.opengauss.batman.modules.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/sys/user")
public class UserController extends AbstractController {
	@Autowired
	private UserService userService;

	/**
	 * 所有用户列表
	 */
	@GetMapping("/list")
	public R list(@RequestParam Map<String, Object> params){
		//只有超级管理员，才能查看所有管理员列表
		if (getUserId() != Const.SUPER_ADMIN) {
			params.put("createUserId", getUserId());
		}
		PageUtils page = userService.queryPage(params);

		return R.ok().put("page", page);
	}
	
	/**
	 * 获取登录的用户信息
	 */
	@GetMapping("/info")
	public R info(){
		return R.ok().put("user", getUser());
	}
	
	/**
	 * 修改登录用户密码
	 */
	@SysLog("修改密码")
	@PostMapping("/password")
	public R password(@RequestBody PasswordForm form){
		Assert.isBlank(form.getNewPassword(), "新密码不为能空");
		
		//sha256加密
		String password = new Sha256Hash(form.getPassword(), getUser().getSalt()).toHex();
		//sha256加密
		String newPassword = new Sha256Hash(form.getNewPassword(), getUser().getSalt()).toHex();

		return userService.updatePassword(getUserId(), password, newPassword) ?
			R.ok() : R.error("原密码不正确");
	}
	
	/**
	 * 用户信息
	 */
	@GetMapping("/info/{userId}")
	public R info(@PathVariable("userId") Long userId){
		UserEntity user = userService.getById(userId);
		return R.ok().put("user", user);
	}
	
	/**
	 * 保存用户
	 */
	@SysLog("保存用户")
	@PostMapping("/save")
	public R save(@RequestBody UserEntity user){
		user.setCreateUserId(getUserId());
		userService.saveUser(user);
		
		return R.ok();
	}
	
	/**
	 * 修改用户
	 */
	@SysLog("修改用户")
	@PostMapping("/update")
	public R update(@RequestBody UserEntity user){
		user.setCreateUserId(getUserId());
		userService.update(user);
		
		return R.ok();
	}
}
