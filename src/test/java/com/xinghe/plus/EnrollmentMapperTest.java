package com.xinghe.plus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghe.entity.Course;
import com.xinghe.entity.Enrollment;
import com.xinghe.entity.Student;
import com.xinghe.plus.mapper.CourseMapper;
import com.xinghe.plus.mapper.EnrollmentMapper;
import com.xinghe.plus.mapper.StudentMapper;
import com.xinghe.support.TestDataCleaner;
import com.xinghe.util.MyBatisPlusUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentMapperTest {

    private static final String SEMESTER = "2026-2027-1";

    private final TestDataCleaner testData = new TestDataCleaner();
    private SqlSession session;
    private EnrollmentMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisPlusUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(EnrollmentMapper.class);
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
        Enrollment enrollment = newEnrollment(insertStudent(), insertCourse(), SEMESTER);
        assertEquals(1, mapper.insert(enrollment));
        session.commit();
        assertNotNull(enrollment.getId());
        assertEquals(enrollment, mapper.selectById(enrollment.getId()));
    }

    @Test
    void testSelectById() {
        Enrollment enrollment = insertEnrollment(insertStudent(), insertCourse(), SEMESTER);
        assertEquals(enrollment, mapper.selectById(enrollment.getId()));
    }

    @Test
    void testUpdateById() {
        Enrollment enrollment = insertEnrollment(insertStudent(), insertCourse(), SEMESTER);
        Enrollment update = new Enrollment();
        update.setId(enrollment.getId());
        update.setScore(new BigDecimal("0.00"));
        assertEquals(1, mapper.updateById(update));
        session.commit();
        enrollment.setScore(update.getScore());
        assertEquals(enrollment, mapper.selectById(enrollment.getId()), "零分有效，未传字段保持原值");
    }

    @Test
    void testDeleteById() {
        Enrollment enrollment = insertEnrollment(insertStudent(), insertCourse(), SEMESTER);
        assertEquals(1, mapper.deleteById(enrollment.getId()));
        session.commit();
        assertNull(mapper.selectById(enrollment.getId()));
    }

    @Test
    void testSelectPage() {
        Integer studentId = insertStudent();
        List<Enrollment> records = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            records.add(insertEnrollment(studentId, insertCourse(), SEMESTER));
        }
        insertEnrollment(studentId, insertCourse(), "2026-2027-2");
        insertEnrollment(insertStudent(), insertCourse(), SEMESTER);

        LambdaQueryWrapper<Enrollment> query = new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getStudentId, studentId)
                .eq(Enrollment::getSemester, SEMESTER)
                .orderByAsc(Enrollment::getId);
        Page<Enrollment> firstPage = mapper.selectPage(new Page<>(1, 5), query);
        assertEquals(records.subList(0, 5), firstPage.getRecords());
        assertEquals(6, firstPage.getTotal());
        assertEquals(1, firstPage.getCurrent());
        assertEquals(5, firstPage.getSize());
        assertEquals(2, firstPage.getPages());

        Page<Enrollment> secondPage = mapper.selectPage(new Page<>(2, 5), query);
        assertEquals(records.subList(5, 6), secondPage.getRecords());
        assertEquals(6, secondPage.getTotal());
        assertEquals(2, secondPage.getCurrent());
        assertEquals(5, secondPage.getSize());
        assertEquals(2, secondPage.getPages());
    }

    private Enrollment insertEnrollment(Integer studentId, Integer courseId, String semester) {
        Enrollment enrollment = newEnrollment(studentId, courseId, semester);
        assertEquals(1, mapper.insert(enrollment));
        session.commit();
        return enrollment;
    }

    // 只构造对象，不执行数据库操作。
    private Enrollment newEnrollment(Integer studentId, Integer courseId, String semester) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setSemester(semester);
        enrollment.setSelectedAt(LocalDateTime.of(2026, 9, 2, 9, 0));
        return enrollment;
    }

    private Integer insertStudent() {
        Student student = new Student();
        testData.track(student);
        student.setStudentNo(UUID.randomUUID().toString().substring(0, 20));
        student.setName("测试学生");
        student.setGender("男");
        student.setMajor("软件工程");
        student.setGrade((short) 2026);
        student.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        assertEquals(1, session.getMapper(StudentMapper.class).insert(student));
        session.commit();
        return student.getId();
    }

    private Integer insertCourse() {
        Course course = new Course();
        testData.track(course);
        course.setCourseNo(UUID.randomUUID().toString().substring(0, 20));
        course.setName("测试课程");
        course.setCredit(new BigDecimal("2.0"));
        course.setHours((short) 32);
        course.setTeacher("测试教师");
        assertEquals(1, session.getMapper(CourseMapper.class).insert(course));
        session.commit();
        return course.getId();
    }
}
