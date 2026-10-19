package com.xinghe.plus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinghe.entity.Course;
import com.xinghe.plus.mapper.CourseMapper;
import com.xinghe.support.TestDataCleaner;
import com.xinghe.util.MyBatisPlusUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CourseMapperTest {

    private final TestDataCleaner testData = new TestDataCleaner();
    private SqlSession session;
    private CourseMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisPlusUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(CourseMapper.class);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (SqlSession currentSession = session) {
            if (currentSession != null) {
                currentSession.rollback(); // 仅回滚异常中断时尚未提交的操作。
            }
        } finally {
            testData.clean(MyBatisPlusUtil.getSqlSessionFactory());
        }
    }

    @Test
    void testInsert() {
        Course course = newCourse("数据库", "李老师", "2.0");
        assertEquals(1, mapper.insert(course));
        session.commit();
        assertNotNull(course.getId());
        assertEquals(course, mapper.selectById(course.getId()));
    }

    @Test
    void testSelectById() {
        Course course = insertCourse("数据库", "李老师", "2.0");
        assertEquals(course, mapper.selectById(course.getId()));
    }

    @Test
    void testUpdateById() {
        Course course = insertCourse("数据库", "李老师", "2.0");
        Course update = new Course();
        update.setId(course.getId());
        update.setTeacher("王老师");
        assertEquals(1, mapper.updateById(update));
        session.commit();
        course.setTeacher(update.getTeacher());
        assertEquals(course, mapper.selectById(course.getId()), "未传字段保持原值");
    }

    @Test
    void testDeleteById() {
        Course course = insertCourse("数据库", "李老师", "2.0");
        assertEquals(1, mapper.deleteById(course.getId()));
        session.commit();
        assertNull(mapper.selectById(course.getId()));
    }

    @Test
    void testSelectList() {
        String token = UUID.randomUUID().toString().substring(0, 8);
        // Wrapper 不会自动去除空白，由调用方先整理输入。
        String name = ("  课" + token + "  ").trim();
        String teacher = ("  师" + token + "  ").trim();
        BigDecimal minimumCredit = new BigDecimal("3.0");
        Course first = insertCourse("前" + name + "后", "甲" + teacher + "乙", "3.0");
        Course second = insertCourse("应用" + name, teacher + "老师", "4.0");
        insertCourse("其他课程", teacher, "3.0");
        insertCourse(name, "其他教师", "3.0");
        Course lowCredit = insertCourse(name, teacher, "2.0");

        LambdaQueryWrapper<Course> query = new LambdaQueryWrapper<Course>()
                .like(name != null && !name.isBlank(), Course::getName, name)
                .like(teacher != null && !teacher.isBlank(), Course::getTeacher, teacher)
                .ge(minimumCredit != null, Course::getCredit, minimumCredit)
                .orderByAsc(Course::getId);
        assertEquals(List.of(first, second), mapper.selectList(query), "三个条件同时成立，并按编号排序");

        minimumCredit = null;
        LambdaQueryWrapper<Course> withoutCredit = new LambdaQueryWrapper<Course>()
                .like(name != null && !name.isBlank(), Course::getName, name)
                .like(teacher != null && !teacher.isBlank(), Course::getTeacher, teacher)
                .ge(minimumCredit != null, Course::getCredit, minimumCredit)
                .orderByAsc(Course::getId);
        assertEquals(List.of(first, second, lowCredit), mapper.selectList(withoutCredit), "未传学分时不添加该条件");
    }

    private Course insertCourse(String name, String teacher, String credit) {
        Course course = newCourse(name, teacher, credit);
        assertEquals(1, mapper.insert(course));
        session.commit();
        return course;
    }

    private Course newCourse(String name, String teacher, String credit) {
        Course course = new Course();
        testData.track(course);
        course.setCourseNo(UUID.randomUUID().toString().substring(0, 20));
        course.setName(name);
        course.setCredit(new BigDecimal(credit));
        course.setHours((short) 32);
        course.setTeacher(teacher);
        return course;
    }
}
