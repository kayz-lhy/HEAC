package com.kayz.heac.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 *
 * @param <T> 业务数据类型 (可以是 Object, List, Page 等)
 */
@Data
@Accessors(chain = true) // 支持链式调用
@Schema(description = "统一响应结构")
@JsonInclude(JsonInclude.Include.NON_NULL) // 如果字段为null，序列化时忽略(如 data为null时不返回data字段)
public class HeacResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码 (200:成功, 500:系统异常, 4xxxx:业务异常)
     */
    @Schema(description = "业务状态码 (200表示成功)")
    private int code;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息")
    private String msg;

    /**
     * 业务数据 (泛型)
     */
    @Schema(description = "业务数据")
    private T data;

    /**
     * 链路追踪ID (用于日志排查)
     */
    @Schema(description = "请求追踪ID")
    private String traceId;

    /**
     * 时间戳
     */
    @Schema(description = "响应时间戳")
    private long timestamp;

    // --- 构造方法私有化 ---
    private HeacResponse() {
        this.timestamp = System.currentTimeMillis();
        // 自动从 MDC 获取 TraceId (前提：网关或拦截器里塞进去了)
        this.traceId = MDC.get("traceId");
    }

    // ==================== 成功响应工厂 ====================

    /**
     * 成功，无数据 (用于 update/delete/void 接口)
     */
    public static <T> HeacResponse<T> success() {
        return success(null);
    }

    /**
     * 成功，有数据
     */
    public static <T> HeacResponse<T> success(T data) {
        HeacResponse<T> r = new HeacResponse<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 成功，自定义消息 (极少用)
     */
    public static <T> HeacResponse<T> success(T data, String msg) {
        HeacResponse<T> r = new HeacResponse<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    // ==================== 失败响应工厂 ====================

    /**
     * 系统级错误 (500)
     */
    public static <T> HeacResponse<T> error(String msg) {
        return error(500, msg);
    }

    /**
     * 业务级错误 (自定义码)
     */
    public static <T> HeacResponse<T> error(int code, String msg) {
        HeacResponse<T> r = new HeacResponse<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
