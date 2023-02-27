package org.opengauss.batman.modules.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("sys_encrypt")
public class PasswordEntity {

    @TableId
    private Long id;

    private Long instanceId;

    private String encryptKey;

    private String encryptIv;
}
