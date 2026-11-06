package top.cch.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.cch.mybatis.entity.Order;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
