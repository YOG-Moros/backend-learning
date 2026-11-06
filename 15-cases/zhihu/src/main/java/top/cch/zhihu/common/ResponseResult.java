package top.cch.zhihu.common;

import lombok.Data;

/**
 * 统一响应体，前端统一按 { code, message, data } 解析
 *
 * @author mqxu
 */
@Data
public class ResponseResult<T> {

    /** 200 成功，500 失败 */
    private Integer code;
    private String message;
    private T data;

    private ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseResult<T> ok() {
        return ok(null);
    }

    public static <T> ResponseResult<T> ok(T data) {
        return new ResponseResult<>(200, "ok", data);
    }

    public static <T> ResponseResult<T> fail(String message) {
        return new ResponseResult<>(500, message, null);
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }
}
