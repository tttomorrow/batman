package org.opengauss.batman.modules.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 数据库实例信息
 */
@Data
@TableName("sys_instance")
public class InstanceEntity {
	@TableId
	private Long id;

	@NotBlank(message="实例名不能为空")
	private String instanceName;

	private String instanceVersion;

	@NotBlank(message="IP地址不能为空")
	private String instanceIp;

	private int instancePort;

	@NotBlank(message="OS用户名不能为空")
	private String osUser;

	@NotBlank(message="OS用户密码不能为空")
	private String osPassword;

	@NotBlank(message="备份路径不能为空")
	private String backupPath;

	private String dbDataPath;

	private int dbPort;

	private Date createTime;

	private String remark;
}
