package com.kayz.heac.opinion.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "发表观点请求参数")
public class OpinionPostDTO implements Serializable {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "事件ID不能为空")
    private String eventId;

    @Schema(description = "立场 (0:中立, 1:支持, 2:反对)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "立场不能为空")
    @Min(value = 0, message = "立场非法")
    @Max(value = 2, message = "立场非法")
    private Integer standpoint;

    @Schema(description = "标签 (1-3个)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "至少选择一个标签")
    @Size(max = 3, message = "最多选择3个标签")
    private List<@NotBlank(message = "标签不能为空") String> tags;

    @Schema(description = "内容 (选填, max 140)")
    @Length(max = 140, message = "内容过长")
    private String content;
}
