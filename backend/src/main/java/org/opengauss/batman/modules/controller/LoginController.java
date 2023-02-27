/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.controller;

import org.opengauss.batman.common.annotation.SysLog;
import org.opengauss.batman.common.utils.R;
import org.opengauss.batman.modules.entity.UserEntity;
import org.opengauss.batman.modules.form.SysLoginForm;
import org.opengauss.batman.modules.service.CaptchaService;
import org.opengauss.batman.modules.service.UserService;
import org.opengauss.batman.modules.service.UserTokenService;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * 登录相关
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
public class LoginController extends AbstractController {
	@Autowired
	private UserService userService;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private CaptchaService captchaService;

	/**
	 * 验证码
	 */
	@GetMapping("captcha")
	public void captcha(HttpServletResponse response, String uuid)throws IOException {
		response.setHeader("Cache-Control", "no-store, no-cache");
		response.setContentType("image/jpeg");

		//获取图片验证码
		BufferedImage image = captchaService.getCaptcha(uuid);

		ServletOutputStream out = response.getOutputStream();
		ImageIO.write(image, "jpg", out);
		IOUtils.closeQuietly(out);
	}

	/**
	 * 登录
	 */
	@SysLog("用户登录")
	@PostMapping("/sys/login")
	public Map<String, Object> login(@RequestBody SysLoginForm form)throws IOException {
		boolean captcha = captchaService.validate(form.getUuid(), form.getCaptcha());
		if(!captcha){
			return R.error("验证码不正确");
		}

		//用户信息
		UserEntity user = userService.queryByUserName(form.getUsername());

		//账号不存在、密码错误
		if(user == null || !user.getPassword().equals(new Sha256Hash(form.getPassword(), user.getSalt()).toHex())) {
			return R.error("账号或密码不正确");
		}

		//账号锁定
		if(user.getStatus() == 0){
			return R.error("账号已被锁定,请联系管理员");
		}

		//生成token，并保存到数据库
		R r = userTokenService.createToken(user.getUserId());
		return r;
	}


	/**
	 * 退出
	 */
	@SysLog("用户退出")
	@PostMapping("/sys/logout")
	public R logout() {
		userTokenService.logout(getUserId());
		return R.ok();
	}
}
