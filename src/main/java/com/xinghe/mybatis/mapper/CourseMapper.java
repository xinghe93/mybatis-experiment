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
    int insert(Course course);

    @Select("SELECT id, course_no, name, credit, hours, teacher FROM course WHERE id = #{id}")
    Course selectById(Integer id);

    @Select("SELECT id, course_no, name, credit, hours, teacher FROM course")
    List<Course> selectAll();

    @Update("""
            <script>
                UPDATE course
                <set>
                    <if test="courseNo != null">
                        course_no = #{courseNo},
                    </if>
                    <if test="name != null">
                        name = #{name},
                    </if>
                    <if test="credit != null">
                        credit = #{credit},
                    </if>
                    <if test="hours != null">
                        hours = #{hours},
                    </if>
                    <if test="teacher != null">
                        teacher = #{teacher},
                    </if>
                </set>
                WHERE id = #{id}
            </script>
            """)
    int updateById(Course course);

    @Delete("DELETE FROM course WHERE id = #{id}")
    int deleteById(Integer id);

}
