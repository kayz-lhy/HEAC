package com.kayz.heac.event.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kayz.heac.common.entity.PageResult;


public class PageUtils {
    private PageUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> PageResult<T> toPageResult(Page<T> page) {
        return PageResult.of(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }
}
