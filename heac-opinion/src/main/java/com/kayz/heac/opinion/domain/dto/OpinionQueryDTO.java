package com.kayz.heac.opinion.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "观点列表查询条件")
public class OpinionQueryDTO implements Serializable {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "必须指定查询的事件")
    private String eventId;

    @Schema(description = "立场筛选 (不传查所有)", example = "1")
    @Min(0)
    @Max(2)
    private Integer standpoint;

    @Schema(description = "页码 (默认1)", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private int page = 1;

    @Schema(description = "每页条数 (默认20, 最大100)", defaultValue = "20")
    @Min(value = 1, message = "条数最小为1")
    @Max(value = 100, message = "每页最多查询100条") // 保护 DB/Redis
    private int size = 20;
}
