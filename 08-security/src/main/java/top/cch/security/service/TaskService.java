package top.cch.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import top.cch.security.common.BusinessException;
import top.cch.security.common.ErrorCode;
import top.cch.security.dto.TaskCreateRequest;
import top.cch.security.dto.TaskReviewRequest;
import top.cch.security.entity.SecTask;
import top.cch.security.entity.SecUser;
import top.cch.security.mapper.SecTaskMapper;
import top.cch.security.mapper.SecUserMapper;
import top.cch.security.model.LoginUser;
import top.cch.security.vo.TaskVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author cch
 * @date 2026/10/28
 * @description TaskService 任务业务：提交 / 我的 / 全班 / 详情 / 批改。
 *              关键规则：归属只从令牌取；详情做归属比对（越权真实 403）；
 *              有待批改任务不能重复提交（30011）；已批改不能重复批改（30012）
 **/
@Service
@RequiredArgsConstructor
public class TaskService {

    /** 与表注释、种子数据一致：SUBMITTED 待批改 / REVIEWED 已批改 */
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_REVIEWED = "REVIEWED";

    private final SecTaskMapper taskMapper;
    private final SecUserMapper userMapper;

    /** 学生提交任务：owner 从令牌取，请求体里塞什么都不生效；同名任务待批改期间重复提交返回 30011 */
    public TaskVO submit(LoginUser user, TaskCreateRequest request) {
        Long pending = taskMapper.selectCount(new LambdaQueryWrapper<SecTask>()
                .eq(SecTask::getOwnerId, user.getId())
                .eq(SecTask::getTitle, request.getTitle())
                .eq(SecTask::getStatus, STATUS_SUBMITTED));
        if (pending != null && pending > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_SUBMIT);
        }
        SecTask task = new SecTask();
        task.setOwnerId(user.getId());
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setStatus(STATUS_SUBMITTED);
        task.setSubmittedAt(LocalDateTime.now());
        taskMapper.insert(task);
        return toVO(task, user.getUsername(), user.getNickname());
    }

    /** 我的提交：只返回自己的，条件从令牌取，URL 里没有用户名可改 */
    public List<TaskVO> mine(LoginUser user) {
        return toVOs(taskMapper.selectList(new LambdaQueryWrapper<SecTask>()
                .eq(SecTask::getOwnerId, user.getId())
                .orderByDesc(SecTask::getSubmittedAt)));
    }

    /** 全班提交列表：教师 / 管理员用（Controller 已由权限点 task:review 把关） */
    public List<TaskVO> listAll() {
        return toVOs(taskMapper.selectList(
                new LambdaQueryWrapper<SecTask>().orderByDesc(SecTask::getSubmittedAt)));
    }

    /** 任务详情：本人或持有 task:review 权限点，否则越权 403（判断在服务层，换个入口也绕不过） */
    public TaskVO detail(LoginUser user, Long id) {
        SecTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (!user.getId().equals(task.getOwnerId())
                && !user.getPermissions().contains("task:review")) {
            throw new AccessDeniedException(
                    "用户 " + user.getUsername() + " 无权访问他人任务 id=" + id);
        }
        return toVOs(List.of(task)).get(0);
    }

    /** 教师批改：已批改的不能重复批改 */
    public TaskVO review(Long id, TaskReviewRequest request) {
        SecTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (STATUS_REVIEWED.equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }
        task.setStatus(STATUS_REVIEWED);
        task.setScore(request.getScore());
        task.setComment(request.getComment());
        task.setReviewedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return toVOs(List.of(task)).get(0);
    }

    /** 批量转 VO：一次查齐归属人，用户名与昵称都要给前端 */
    private List<TaskVO> toVOs(List<SecTask> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }
        List<Long> ownerIds = tasks.stream().map(SecTask::getOwnerId).distinct().toList();
        Map<Long, SecUser> userById = userMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(SecUser::getId, Function.identity(), (a, b) -> a));
        return tasks.stream()
                .map(t -> toVO(t,
                        userById.get(t.getOwnerId()).getUsername(),
                        userById.get(t.getOwnerId()).getNickname()))
                .toList();
    }

    private TaskVO toVO(SecTask task, String owner, String ownerNickname) {
        return new TaskVO(task.getId(), task.getTitle(), task.getContent(),
                task.getOwnerId(), owner, ownerNickname,
                task.getStatus(), statusText(task.getStatus()),
                task.getScore(), task.getComment(),
                task.getSubmittedAt(), task.getReviewedAt());
    }

    /** 状态中文由后端统一给出，前端直接显示 */
    private String statusText(String status) {
        return STATUS_SUBMITTED.equals(status) ? "待批改" : "已批改";
    }
}
