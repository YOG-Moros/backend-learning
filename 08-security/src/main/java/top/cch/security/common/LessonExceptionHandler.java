package top.cch.security.common;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import top.cch.security.controller.*;

/** 新课程接口按 HTTP 状态返回错误，保留旧业务接口的历史业务码契约。 */
@Order(-1)
@RestControllerAdvice(assignableTypes = {LessonAuthController.class, StageController.class})
public class LessonExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> authentication(AuthenticationException e) {
        int status = e instanceof AccountStatusException ? 403 : 401;
        String message = e instanceof DisabledException ? "账号已被禁用" :
                e instanceof LockedException ? "账号已被锁定" : "用户名、密码或验证码错误";
        return ResponseEntity.status(status).body(new Result<>(status, message, null));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new Result<>(e.getStatusCode().value(), e.getReason(), null));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result<Void>> invalid(Exception e) {
        return ResponseEntity.badRequest().body(new Result<>(400, "请求参数格式错误", null));
    }
}
