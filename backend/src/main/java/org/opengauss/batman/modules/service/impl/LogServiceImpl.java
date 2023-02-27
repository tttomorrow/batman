/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.batman.common.utils.PageUtils;
import org.opengauss.batman.common.utils.Query;
import org.opengauss.batman.modules.dao.SysLogDao;
import org.opengauss.batman.modules.entity.LogEntity;
import org.opengauss.batman.modules.service.LogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("sysLogService")
public class LogServiceImpl extends ServiceImpl<SysLogDao, LogEntity> implements LogService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String)params.get("key");

        IPage<LogEntity> page = this.page(
            new Query<LogEntity>().getPage(params),
            new QueryWrapper<LogEntity>().like(StringUtils.isNotBlank(key),"username", key)
        );

        return new PageUtils(page);
    }
}
