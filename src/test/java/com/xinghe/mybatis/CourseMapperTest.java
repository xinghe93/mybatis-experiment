package com.xinghe.mybatis;

import com.xinghe.entity.Course;
import com.xinghe.mybatis.mapper.CourseMapper;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourseMapperTest {

    private SqlSession sqlSession;
    private CourseMapper courseMapper;

    @BeforeEach
    void openSession() {
        sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
        courseMapper = sqlSession.getMapper(CourseMapper.class);
    }

    @AfterEach
    void rollbackAndCloseSession() {
        if (sqlSession != null) {
            try {
                // 即使断言失败也回滚，测试不永久修改实验数据。
                sqlSession.rollback();
            } finally {
                sqlSession.close();
            }
        }
    }

    // 新增课程，并检查主键回填。
    @Test
    void testInsert() {
        Course course = newTestCourse();

        assertEquals(1, courseMapper.insert(course));
        assertNotNull(course.getId());
    }

    // 根据 ID 查询课程。
    @Test
    void testSelectById() {
        Course course = insertTestCourse();

        assertEquals(course, courseMapper.selectById(course.getId()));
    }

    // 查询全部课程，结果应包含刚新增的课程。
    @Test
    void testSelectAll() {
        Course course = insertTestCourse();

        assertTrue(courseMapper.selectAll().contains(course));
    }

    // 只修改教师，其他字段保持原值。
    @Test
    void testUpdateById() {
        Course course = insertTestCourse();

        Course update = new Course();
        update.setId(course.getId());
        update.setTeacher("新教师");
        assertEquals(1, courseMapper.updateById(update));

        course.setTeacher("新教师");
        assertEquals(course, courseMapper.selectById(course.getId()));
    }

    // 删除课程，删除后应查询不到。
    @Test
    void testDeleteById() {
        Course course = insertTestCourse();

        assertEquals(1, courseMapper.deleteById(course.getId()));
        assertNull(courseMapper.selectById(course.getId()));
    }

    /** 构造临时课程对象，不写入数据库。 */
    private Course newTestCourse() {
        Course course = new Course();
        course.setCourseNo("test" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        course.setName("测试课程");
        course.setCredit(new BigDecimal("2.0"));
        course.setHours((short) 32);
        course.setTeacher("原教师");
        return course;
    }

    /** 插入临时课程，并检查影响行数和主键回填。 */
    private Course insertTestCourse() {
        Course course = newTestCourse();
        assertEquals(1, courseMapper.insert(course));
        assertNotNull(course.getId());
        return course;
    }

}
