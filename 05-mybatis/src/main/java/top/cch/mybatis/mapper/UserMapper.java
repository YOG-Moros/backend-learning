package top.cch.mybatis.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.cch.mybatis.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
