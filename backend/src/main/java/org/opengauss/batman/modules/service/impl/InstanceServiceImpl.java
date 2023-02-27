package org.opengauss.batman.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.batman.common.ErrorCode;
import org.opengauss.batman.common.exception.BatException;
import org.opengauss.batman.common.utils.AESUtils;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.Query;
import org.opengauss.batman.common.utils.SSHUtils;
import org.opengauss.batman.modules.dao.SysEncryptDao;
import org.opengauss.batman.modules.dao.SysInstanceDao;
import org.opengauss.batman.modules.entity.InstanceEntity;
import org.opengauss.batman.modules.entity.PasswordEntity;
import org.opengauss.batman.modules.service.InstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("sysInstanceService")
public class InstanceServiceImpl extends ServiceImpl<SysInstanceDao, InstanceEntity> implements InstanceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstanceServiceImpl.class);

	private static final Pattern INSTANCE_INFO_PATTERN =
		Pattern.compile("(\\d+)\\s*\\d+\\s*(\\/([\\w-_]+\\/?)*)\\s*P\\s*Primary");

	@Autowired
	private SysEncryptDao sysEncryptDao;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		IPage<InstanceEntity> page = this.page(
			new Query<InstanceEntity>().getPage(params),
			new QueryWrapper<>()
		);

		return new PageUtils(page);
	}

	private void updateInstanceInfo(InstanceEntity instance, String input) {
		String[] lines = StringUtils.split(input, "\n");
		if (ArrayUtils.isEmpty(lines)) {
			LOGGER.error("input info error: {}", input);
			throw new BatException(ErrorCode.PARSE_INSTANCE_INFO_ERROR);
		}
		for (String line : lines) {
			Matcher matcher = INSTANCE_INFO_PATTERN.matcher(line);
			if (matcher.find()) {
				instance.setDbPort(Integer.parseInt(matcher.group(1)));
				instance.setDbDataPath(matcher.group(2));
				return;
			}
		}
		LOGGER.error("can not parse primary node data path from : {}", input);
		throw new BatException(ErrorCode.PARSE_INSTANCE_INFO_ERROR);
	}

	private void checkAndUpdateInstanceInfo(InstanceEntity instance) {
		Session session = SSHUtils.getSession(instance.getInstanceIp(), instance.getInstancePort(),
				instance.getOsUser(), instance.getOsPassword());
		String remoteCmd = "gs_om --version | awk  '{print $4}'";
		String cmdResult = SSHUtils.execRemoteCmd(session, remoteCmd, "");
		if (StringUtils.isEmpty(cmdResult)) {
			LOGGER.error("get instance version failed");
			throw new BatException(ErrorCode.INSTANCE_VERSION_ERROR);
		}
		instance.setInstanceVersion(StringUtils.trim(cmdResult));

		remoteCmd = "gs_om -t status --detail";
		cmdResult = SSHUtils.execRemoteCmd(session, remoteCmd, "\n");

		updateInstanceInfo(instance, cmdResult);

		StringBuilder cmdBuilder = new StringBuilder();
		cmdBuilder.append("if [ ! -d ").append(instance.getBackupPath())
				.append(" ]; then mkdir -p ").append(instance.getBackupPath())
				.append("; fi && ls -al ").append(instance.getBackupPath());
		cmdResult = SSHUtils.execRemoteCmd(session, cmdBuilder.toString(), "");
		if (StringUtils.isBlank(cmdResult)) {
			LOGGER.error("check backup path failed");
			throw new BatException(ErrorCode.BACKUP_PATH_ERROR);
		}
		session.disconnect();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> newInstance(InstanceEntity instance) throws BatException {
		checkAndUpdateInstanceInfo(instance);
		String encryptKey = AESUtils.genEncryptKey();
		String encryptIV = AESUtils.genEncryptIV();
		String encryptedPassword = AESUtils.encrypt(instance.getOsPassword(), encryptKey, encryptIV);

		instance.setOsPassword(encryptedPassword);
		instance.setCreateTime(new Date());
		baseMapper.insertNewInstance(instance);
		InstanceEntity instanceWithId = baseMapper.queryInstanceByName(instance.getInstanceName());

		PasswordEntity encryptEntity = PasswordEntity.builder()
			.instanceId(instanceWithId.getId())
			.encryptKey(encryptKey)
			.encryptIv(encryptIV).build();
		sysEncryptDao.saveEncryptRecord(encryptEntity);

		Map<String, Object> result = new HashMap<>();
		result.put("version", instance.getInstanceVersion());
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateInstance(InstanceEntity newInstance) throws BatException {
		checkAndUpdateInstanceInfo(newInstance);
		this.updateById(newInstance);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBatch(Long[] ids) {
		sysEncryptDao.deleteByInstanceId(Arrays.asList(ids));
		this.removeByIds(Arrays.asList(ids));
	}

	@Override
	public InstanceEntity getInstanceById(long instanceId) {
		return baseMapper.queryInstanceById(instanceId);
	}

	public String getPlainPassword(String password, long instanceId) {
		PasswordEntity entity = sysEncryptDao.queryEntityById(instanceId);
		return AESUtils.decrypt(password, entity.getEncryptKey(), entity.getEncryptIv());
	}
}
