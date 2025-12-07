package com.kayz.heac.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 标准分页数据结构
 * 解耦具体的持久层框架 (MyBatis Plus / JPA)
 */
@Data
@NoArgsConstructor(staticName = "empty")
@AllArgsConstructor(staticName = "of")
@Builder
public class PageResult<T> implements Serializable {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long current;

    @Schema(description = "每页大小")
    private long size;

    // 辅助方法：从 MyBatis Plus Page 对象转换
    // 如果你不希望 common 依赖 mybatis-plus，这个方法可以写在 Util 里
    /*
    public static <T> PageResult<T> restPage(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
    */
}
