package com.xinghe.mybatis;

import com.xinghe.entity.Course;
import com.xinghe.entity.Enrollment;
import com.xinghe.entity.Student;
import com.xinghe.mybatis.mapper.CourseMapper;
import com.xinghe.mybatis.mapper.EnrollmentMapper;
import com.xinghe.mybatis.mapper.StudentMapper;
import com.xinghe.support.TestDataCleaner;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        session = MyBatisUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(EnrollmentMapper.class);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (SqlSession currentSession = session) {
            if (currentSession != null) {
                currentSession.rollback(); // 仅回滚异常中断时尚未提交的操作。
            }
        } finally {
            testData.clean(MyBatisUtil.getSqlSessionFactory());
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
        assertEquals(enrollment, mapper.selectById(enrollment.getId()), "零分有效，其他字段保持原值");
    }

    @Test
    void testDeleteById() {
        Enrollment enrollment = insertEnrollment(insertStudent(), insertCourse(), SEMESTER);
        assertEquals(1, mapper.deleteById(enrollment.getId()));
        session.commit();
        assertNull(mapper.selectById(enrollment.getId()));
    }

    @Test
    void testInsertBatch() {
        Integer studentId = insertStudent();
        Enrollment first = newEnrollment(studentId, insertCourse(), SEMESTER);
        Enrollment second = newEnrollment(studentId, insertCourse(), SEMESTER);
        second.setScore(new BigDecimal("0.00"));
        assertEquals(2, mapper.insertBatch(List.of(first, second)));
        session.commit();

        List<Enrollment> records = mapper.selectByStudentAndSemester(first.getStudentId(), first.getSemester());
        assertEquals(List.of(first.getCourseId(), second.getCourseId()),
                records.stream().map(Enrollment::getCourseId).toList());
        assertNull(records.get(0).getScore());
        assertEquals(new BigDecimal("0.00"), records.get(1).getScore());
    }

    @Test
    void testSelectByStudentAndSemester() {
        Integer studentId = insertStudent();
        Enrollment expected = insertEnrollment(studentId, insertCourse(), SEMESTER);
        insertEnrollment(studentId, insertCourse(), "2026-2027-2");
        insertEnrollment(insertStudent(), insertCourse(), SEMESTER);

        assertEquals(List.of(expected), mapper.selectByStudentAndSemester(studentId, SEMESTER));
        assertTrue(mapper.selectByStudentAndSemester(studentId, "2026").isEmpty());
    }

    @Test
    void testDeleteBatch() {
        Integer studentId = insertStudent();
        Enrollment first = insertEnrollment(studentId, insertCourse(), SEMESTER);
        Enrollment second = insertEnrollment(studentId, insertCourse(), SEMESTER);
        Enrollment untouched = insertEnrollment(studentId, insertCourse(), SEMESTER);
        assertEquals(0, mapper.deleteBatch(null));
        session.commit();
        assertEquals(0, mapper.deleteBatch(List.of()));
        session.commit();
        assertEquals(first, mapper.selectById(first.getId()));

        assertEquals(2, mapper.deleteBatch(List.of(first.getId(), second.getId())));
        session.commit();
        assertNull(mapper.selectById(first.getId()));
        assertNull(mapper.selectById(second.getId()));
        assertEquals(untouched, mapper.selectById(untouched.getId()));
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
