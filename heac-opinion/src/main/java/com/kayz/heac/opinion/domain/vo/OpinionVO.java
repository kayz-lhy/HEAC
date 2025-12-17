package com.kayz.heac.opinion.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kayz.heac.common.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "观点展示视图")
public class OpinionVO implements Serializable {

    @Schema(description = "观点ID")
    private String id;

    @Schema(description = "发布者信息")
    private UserInfoVO userInfo;

    @Schema(description = "立场 (0/1/2)")
    private Integer standpoint;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "是否是我的")
    private Boolean isMine;
}
