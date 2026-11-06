package top.cch.doc.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    BOOK_NOT_FOUND(20404, "图书不存在"),
    BOOK_ISBN_DUPLICATED(20409, "ISBN 已存在，请勿重复添加"),
    BAD_REQUEST(400, "请求参数错误"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
