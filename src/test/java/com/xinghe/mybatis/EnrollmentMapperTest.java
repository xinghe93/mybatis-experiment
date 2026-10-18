package com.xinghe.mybatis;

import com.xinghe.entity.Course;
import com.xinghe.entity.Enrollment;
import com.xinghe.entity.Student;
import com.xinghe.mybatis.mapper.CourseMapper;
import com.xinghe.mybatis.mapper.EnrollmentMapper;
import com.xinghe.mybatis.mapper.StudentMapper;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentMapperTest {

    private SqlSession session;
    private EnrollmentMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(EnrollmentMapper.class);
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
        Enrollment enrollment = newEnrollment();
        assertEquals(1, mapper.insert(enrollment));
        assertNotNull(enrollment.getId());
    }

    @Test
    void testSelectById() {
        Enrollment enrollment = insertEnrollment();
        assertEquals(enrollment, mapper.selectById(enrollment.getId()));
    }

    @Test
    void testUpdateById() {
        Enrollment enrollment = insertEnrollment();
        Enrollment update = new Enrollment();
        update.setId(enrollment.getId());
        update.setScore(new BigDecimal("0.00"));
        assertEquals(1, mapper.updateById(update));
        enrollment.setScore(update.getScore());
        assertEquals(enrollment, mapper.selectById(enrollment.getId()), "零分有效，其他字段保持原值");
    }

    @Test
    void testDeleteById() {
        Enrollment enrollment = insertEnrollment();
        assertEquals(1, mapper.deleteById(enrollment.getId()));
        assertNull(mapper.selectById(enrollment.getId()));
    }

    @Test
    void testInsertBatch() {
        Enrollment first = newEnrollment();
        Enrollment second = newEnrollment();
        second.setStudentId(first.getStudentId());
        second.setScore(new BigDecimal("0.00"));
        assertEquals(2, mapper.insertBatch(List.of(first, second)));

        List<Enrollment> records = mapper.selectByStudentAndSemester(first.getStudentId(), first.getSemester());
        assertEquals(List.of(first.getCourseId(), second.getCourseId()),
                records.stream().map(Enrollment::getCourseId).toList());
        assertNull(records.get(0).getScore());
        assertEquals(new BigDecimal("0.00"), records.get(1).getScore());
    }

    @Test
    void testSelectByStudentAndSemester() {
        Enrollment expected = insertEnrollment();
        Enrollment otherSemester = newEnrollment();
        otherSemester.setStudentId(expected.getStudentId());
        otherSemester.setSemester("2026-2027-2");
        mapper.insert(otherSemester);
        insertEnrollment(); // 同学期的另一名学生不应被查出。

        assertEquals(List.of(expected),
                mapper.selectByStudentAndSemester(expected.getStudentId(), expected.getSemester()));
        assertTrue(mapper.selectByStudentAndSemester(expected.getStudentId(), "2026").isEmpty());
    }

    @Test
    void testDeleteBatch() {
        Enrollment first = insertEnrollment();
        Enrollment second = insertEnrollment();
        Enrollment untouched = insertEnrollment();
        assertEquals(0, mapper.deleteBatch(null));
        assertEquals(0, mapper.deleteBatch(List.of()));
        assertEquals(first, mapper.selectById(first.getId()));

        assertEquals(2, mapper.deleteBatch(List.of(first.getId(), second.getId())));
        assertNull(mapper.selectById(first.getId()));
        assertNull(mapper.selectById(second.getId()));
        assertEquals(untouched, mapper.selectById(untouched.getId()));
    }

    private Enrollment insertEnrollment() {
        Enrollment enrollment = newEnrollment();
        mapper.insert(enrollment);
        return enrollment;
    }

    // 先创建父表数据，满足外键；所有数据由 tearDown 一并回滚。
    private Enrollment newEnrollment() {
        String number = UUID.randomUUID().toString().substring(0, 20);
        Student student = new Student();
        student.setStudentNo(number);
        student.setName("测试学生");
        student.setGender("男");
        student.setMajor("软件工程");
        student.setGrade((short) 2026);
        student.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        assertEquals(1, session.getMapper(StudentMapper.class).insert(student));

        Course course = new Course();
        course.setCourseNo(number);
        course.setName("测试课程");
        course.setCredit(new BigDecimal("2.0"));
        course.setHours((short) 32);
        course.setTeacher("测试教师");
        assertEquals(1, session.getMapper(CourseMapper.class).insert(course));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setSemester("2026-2027-1");
        enrollment.setSelectedAt(LocalDateTime.of(2026, 9, 2, 9, 0));
        return enrollment;
    }
}
