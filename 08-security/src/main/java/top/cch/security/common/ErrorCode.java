package top.cch.security.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author cch
 * @date 2026/10/28
 * @description ErrorCode 统一错误码：200 成功；3xxxx 业务错误；400/401/403/404/500 协议层错误
 **/
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或令牌已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),

    LOGIN_FAILED(30001, "用户名或密码错误"),
    ACCOUNT_DISABLED(30002, "账号已被停用"),
    USER_NOT_FOUND(30003, "用户不存在"),
    USERNAME_EXISTS(30004, "用户名已存在"),
    CANNOT_DELETE_SELF(30005, "不能删除当前登录账号"),

    TASK_NOT_FOUND(30010, "任务不存在"),
    DUPLICATE_SUBMIT(30011, "同一任务已提交，等待批改"),
    DUPLICATE_REVIEW(30012, "该任务已批改，不能重复批改");

    private final Integer code;
    private final String message;
}
