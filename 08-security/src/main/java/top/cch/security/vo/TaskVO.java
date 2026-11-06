package top.cch.security.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cch
 * @date 2026/10/28
 * @description TaskVO 任务视图：归属人用户名 + 昵称 + 状态中文文案，不含敏感字段
 **/
@Data
@AllArgsConstructor
public class TaskVO {

    private Long id;

    private String title;

    private String content;

    /** 归属人 id（sec_user.id） */
    private Long ownerId;

    /** 归属人用户名 */
    private String owner;

    /** 归属人昵称，页面显示用 */
    private String ownerNickname;

    /** SUBMITTED 待批改 / REVIEWED 已批改 */
    private String status;

    /** 状态中文：待批改 / 已批改，前端直接显示不再自己映射 */
    private String statusText;

    /** 分数 0-100，未批改为 null */
    private Integer score;

    /** 评语，未批改为 null */
    private String comment;

    private LocalDateTime submittedAt;

    /** 批改时间，未批改为 null */
    private LocalDateTime reviewedAt;
}
