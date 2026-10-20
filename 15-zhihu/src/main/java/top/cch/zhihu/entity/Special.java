package top.cch.zhihu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 知乎专题实体，对应表 t_special
 *
 * <p>id 是知乎的雪花 ID（长数字字符串），超过 JS 安全整数范围，必须用 String 接收，
 * 否则前端拿到的 id 会丢精度。</p>
 *
 * @author mqxu
 */
@Data
@TableName("t_special")
public class Special implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 专题 id（雪花 ID 字符串） */
    private String id;

    /** 专题标题 */
    private String title;

    /** 专题简介 */
    private String introduction;

    /** 头图地址 */
    private String banner;

    /** 浏览量 */
    private Long viewCount;

    /** 关注量 */
    private Integer followersCount;

    /** 是否已关注：1 是，0 否（前端条件渲染用） */
    private Integer isFollowing;

    /** 分类：热门 / 关注中 / 小众（练习 1 新增字段） */
    private String category;
}
