package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Course;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CourseMapper {

    @Insert("")
    void insert(Course course);

    @Select("")
    Course selectById(Integer id);

    @Select("")
    List<Course> selectAll();

    @Update("")
    void updateById(Course course);

    @Delete("")
    void deleteById(Integer id);

}
