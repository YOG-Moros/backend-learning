package top.cch.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.cch.mybatis.entity.Student;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
