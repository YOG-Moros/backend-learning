package top.cch.mybatis.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.cch.mybatis.entity.Course;
import top.cch.mybatis.mapper.CourseMapper;
import top.cch.mybatis.service.CourseService;

import java.util.List;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveCourses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return false;
        }
        return saveBatch(courses);
    }

    @Override
    public boolean removeCourses(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return removeByIds(ids);
    }

    @Override
    public List<Course> listCourses() {
        return list(Wrappers.<Course>lambdaQuery().orderByAsc(Course::getId));
    }
}
