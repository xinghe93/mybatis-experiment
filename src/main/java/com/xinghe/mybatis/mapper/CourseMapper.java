package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Course;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CourseMapper {

    @Insert("INSERT INTO course (course_no, name, credit, hours, teacher) VALUES (#{courseNo}, #{name}, #{credit}, #{hours}, #{teacher})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Select("SELECT id, course_no, name, credit, hours, teacher FROM course WHERE id = #{id}")
    Course selectById(Integer id);

    /**
     * 调用前校验 ID、名称、学分、学时、教师均非空；固定更新不修改课程代码。
     */
    @Update("UPDATE course SET name = #{name}, credit = #{credit}, hours = #{hours}, teacher = #{teacher} WHERE id = #{id}")
    int updateById(Course course);

    @Delete("DELETE FROM course WHERE id = #{id}")
    int deleteById(Integer id);

    // <choose> 只选择最高优先级的非空条件，即使没有匹配也不回退。
    @Select("""
            <script>
                <bind name="code" value="condition != null and condition.courseNo != null ? condition.courseNo.trim() : null"/>
                <bind name="name" value="condition != null and condition.name != null ? condition.name.trim() : null"/>
                <bind name="teacher" value="condition != null and condition.teacher != null ? condition.teacher.trim() : null"/>
                SELECT id, course_no, name, credit, hours, teacher FROM course
                <choose>
                    <when test="code != null and code != ''">WHERE course_no = #{code}</when>
                    <when test="name != null and name != ''">WHERE name LIKE CONCAT('%', #{name}, '%')</when>
                    <when test="teacher != null and teacher != ''">WHERE teacher LIKE CONCAT('%', #{teacher}, '%')</when>
                    <otherwise>WHERE 1 = 1</otherwise>
                </choose>
                ORDER BY id
            </script>
            """)
    List<Course> selectByPriority(@Param("condition") Course course);

}
