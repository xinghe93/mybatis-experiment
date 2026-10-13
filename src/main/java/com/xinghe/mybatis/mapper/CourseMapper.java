package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Course;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CourseMapper {

    /**
     * 新增课程，并将自增主键回填到 course.id。
     */
    @Insert("INSERT INTO course (course_no, name, credit, hours, teacher) VALUES (#{courseNo}, #{name}, #{credit}, #{hours}, #{teacher})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Course course);

    @Select("SELECT id, course_no, name, credit, hours, teacher FROM course WHERE id = #{id}")
    Course selectById(Integer id);

    @Select("SELECT id, course_no, name, credit, hours, teacher FROM course")
    List<Course> selectAll();

    @Update("")
    void updateById(Course course);

    @Delete("")
    void deleteById(Integer id);

}
