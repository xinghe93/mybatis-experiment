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

    private SqlSession session;
    private CourseMapper courseMapper;
    private Course course;

    @BeforeEach
    void setUp() {
        session = MyBatisUtil.getSqlSessionFactory().openSession();
        courseMapper = session.getMapper(CourseMapper.class);

        course = new Course();
        course.setCourseNo("test" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        course.setName("测试课程");
        course.setCredit(new BigDecimal("2.0"));
        course.setHours((short) 32);
        course.setTeacher("原教师");
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            try {
                // 回滚测试数据，避免重复运行影响数据库。
                session.rollback();
            } finally {
                session.close();
            }
        }
    }

    // 新增课程，并检查主键回填。
    @Test
    void testInsert() {
        assertEquals(1, courseMapper.insert(course));
        assertNotNull(course.getId());
    }

    // 根据 ID 查询课程。
    @Test
    void testSelectById() {
        assertEquals(1, courseMapper.insert(course));

        assertEquals(course, courseMapper.selectById(course.getId()));
    }

    // 查询全部课程，结果应包含刚新增的课程。
    @Test
    void testSelectAll() {
        assertEquals(1, courseMapper.insert(course));

        assertTrue(courseMapper.selectAll().contains(course));
    }

    // 只修改教师，其他字段保持原值。
    @Test
    void testUpdateById() {
        assertEquals(1, courseMapper.insert(course));

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
        assertEquals(1, courseMapper.insert(course));

        assertEquals(1, courseMapper.deleteById(course.getId()));
        assertNull(courseMapper.selectById(course.getId()));
    }

}
