package top.cch.file.common;

public enum ErrorCode {

    FILE_EMPTY(10401, 400, "文件为空"),
    FILE_TOO_LARGE(10403, 413, "文件超过大小限制"),
    FILE_TYPE_NOT_ALLOWED(10402, 400, "文件类型不支持"),
    FILE_NOT_FOUND(10404, 404, "文件不存在或已被删除"),
    STORAGE_ERROR(10500, 500, "存储服务异常"),
    BAD_REQUEST(400, 400, "参数错误");

    private final int code;
    private final int httpStatus;
    private final String message;

    ErrorCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
