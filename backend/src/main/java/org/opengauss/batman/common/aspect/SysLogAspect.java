/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.common.aspect;

import com.alibaba.druid.util.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.opengauss.batman.common.annotation.SysLog;
import org.opengauss.batman.common.utils.HttpContextUtils;
import org.opengauss.batman.common.utils.IPUtils;
import org.opengauss.batman.modules.entity.LogEntity;
import org.opengauss.batman.modules.entity.UserEntity;
import org.opengauss.batman.modules.form.SysLoginForm;
import org.opengauss.batman.modules.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;


/**
 * 系统日志，切面处理类
 *
 * @author Mark sunlightcs@gmail.com
 */
@Aspect
@Component
public class SysLogAspect {

	@Autowired
	private LogService logService;
	
	@Pointcut("@annotation(org.opengauss.batman.common.annotation.SysLog)")
	public void logPointCut() { 
		
	}

	@Around("logPointCut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		//执行方法
		Object result = point.proceed();
		//保存日志
		saveSysLog(point);
		return result;
	}

	private void saveSysLog(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		LogEntity sysLog = new LogEntity();
		SysLog operation = method.getAnnotation(SysLog.class);
		if (operation != null){
			//注解上的描述
			sysLog.setOperation(operation.value());
		}

		//获取request, 设置IP地址
		HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
		sysLog.setLogIp(IPUtils.getIpAddr(request));

		//请求的方法名
		String methodName = signature.getName();
		String username = "";
		if (StringUtils.equalsIgnoreCase(methodName, "login")) {
			SysLoginForm forms = (SysLoginForm)joinPoint.getArgs()[0];
			username = forms.getUsername();
		} else {
			username = ((UserEntity) SecurityUtils.getSubject().getPrincipal()).getUsername();
		}
		sysLog.setUsername(username);

		sysLog.setOperationTime(new Date());
		//保存系统日志
		logService.save(sysLog);
	}
}
