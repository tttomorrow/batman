package org.opengauss.batman.modules.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.batman.modules.entity.PasswordEntity;

import java.util.List;

@Mapper
public interface SysEncryptDao extends BaseMapper<PasswordEntity> {

    boolean saveEncryptRecord(PasswordEntity encryptEntity);

    PasswordEntity queryEntityById(Long instanceId);

    void deleteByInstanceId(List<Long> instanceIds);
}
