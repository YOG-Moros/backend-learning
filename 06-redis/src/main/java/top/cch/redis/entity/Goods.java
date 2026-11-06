package top.cch.redis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private BigDecimal price;

    private Integer stock;

    private Integer sales;

    private Integer status;

    private java.time.LocalDateTime createTime;

    private java.time.LocalDateTime updateTime;
}
