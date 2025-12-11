package com.kayz.heac.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "用户基础信息视图 (脱敏)")
public class UserInfoVO implements Serializable {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "实名认证状态 (UNVERIFIED/VERIFIED)")
    private String realNameStatus;

    @Schema(description = "用户标签 (大V, 媒体)")
    private List<String> tags;

    @Schema(description = "个性化配置 (JSON)")
    private Map<String, Object> preferences;
}
