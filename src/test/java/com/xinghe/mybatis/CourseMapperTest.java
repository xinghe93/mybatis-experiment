package com.xinghe.mybatis;

import com.xinghe.entity.Course;
import com.xinghe.mybatis.mapper.CourseMapper;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CourseMapperTest {

    private SqlSession session;
    private CourseMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(CourseMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            try {
                session.rollback();
            } finally {
                session.close();
            }
        }
    }

    @Test
    void testInsert() {
        Course course = newCourse("数据库", "李老师");
        assertEquals(1, mapper.insert(course));
        assertNotNull(course.getId());
    }

    @Test
    void testSelectById() {
        Course course = insertCourse("数据库", "李老师");
        assertEquals(course, mapper.selectById(course.getId()));
    }

    @Test
    void testUpdateById() {
        Course course = insertCourse("数据库", "李老师");
        course.setName("数据库原理");
        course.setCredit(new BigDecimal("3.0"));
        course.setHours((short) 48);
        course.setTeacher("王老师");
        assertEquals(1, mapper.updateById(course));
        assertEquals(course, mapper.selectById(course.getId()));
    }

    @Test
    void testDeleteById() {
        Course course = insertCourse("数据库", "李老师");
        assertEquals(1, mapper.deleteById(course.getId()));
        assertNull(mapper.selectById(course.getId()));
    }

    @Test
    void testSelectByPriority() {
        String token = UUID.randomUUID().toString().substring(0, 8);
        Course first = insertCourse("数据" + token, "教师" + token);
        Course second = insertCourse("数据" + token, "其他教师");
        Course third = insertCourse("其他课程", "教师" + token);

        Course condition = new Course();
        condition.setCourseNo("  " + first.getCourseNo() + "  ");
        condition.setName("不存在");
        assertEquals(List.of(first), mapper.selectByPriority(condition));

        condition.setCourseNo("  ");
        condition.setName("  " + token + "  ");
        condition.setTeacher("不存在");
        assertEquals(List.of(first, second), mapper.selectByPriority(condition));

        condition.setName("");
        condition.setTeacher("  " + token + "  ");
        assertEquals(List.of(first, third), mapper.selectByPriority(condition));

        condition.setCourseNo(first.getCourseNo().substring(0, 18));
        condition.setName(token);
        assertTrue(mapper.selectByPriority(condition).isEmpty(), "代码精确匹配，未命中也不回退");
        assertTrue(mapper.selectByPriority(null).containsAll(List.of(first, second, third)), "otherwise 查询全部");
    }

    private Course insertCourse(String name, String teacher) {
        Course course = newCourse(name, teacher);
        mapper.insert(course);
        return course;
    }

    private Course newCourse(String name, String teacher) {
        Course course = new Course();
        course.setCourseNo(UUID.randomUUID().toString().substring(0, 20));
        course.setName(name);
        course.setCredit(new BigDecimal("2.0"));
        course.setHours((short) 32);
        course.setTeacher(teacher);
        return course;
    }
}
