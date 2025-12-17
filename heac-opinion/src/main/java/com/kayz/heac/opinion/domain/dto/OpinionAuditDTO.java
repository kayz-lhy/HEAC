package com.kayz.heac.opinion.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "观点审核/管理参数")
public class OpinionAuditDTO implements Serializable {

    @Schema(description = "观点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "ID不能为空")
    private String id;

    @Schema(description = "操作类型 (PASS:通过, REJECT:驳回, HIDE:隐藏)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "操作类型不能为空")
    @Pattern(regexp = "^(PASS|REJECT|HIDE)$", message = "非法操作类型")
    private String action;

    @Schema(description = "驳回/隐藏原因", example = "涉及敏感信息")
    private String reason;
}
