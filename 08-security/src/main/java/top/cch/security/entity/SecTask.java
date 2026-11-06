package top.cch.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cch
 * @date 2026/10/28
 * @description SecTask 实训任务表：归属只存 owner_id，不存用户名
 **/
@Data
@TableName("sec_task")
public class SecTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 归属人（sec_user.id），只从令牌取，绝不从前端传 */
    private Long ownerId;

    /** 标题 ≤100 字 */
    private String title;

    /** 内容 ≤2000 字 */
    private String content;

    /** 状态：PENDING 待批改 / REVIEWED 已批改 */
    private String status;

    /** 分数 0-100，批改后才有 */
    private Integer score;

    /** 评语 ≤500 字 */
    private String comment;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;
}
