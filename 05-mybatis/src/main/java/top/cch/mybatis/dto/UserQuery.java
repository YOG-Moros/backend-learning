package top.cch.mybatis.dto;

import lombok.Data;

/**
 * 查询条件对象，字段全为空时等价于查全部
 */
@Data
public class UserQuery {
    private String keyword;        // 匹配 username 或 nickname
    private Integer gender;
    private String memberLevel;
    private Boolean vip;
    private Integer status;
    private Integer minAge;
    private Integer maxAge;
}
