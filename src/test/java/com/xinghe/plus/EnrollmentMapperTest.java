package com.xinghe.plus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghe.entity.Course;
import com.xinghe.entity.Enrollment;
import com.xinghe.entity.Student;
import com.xinghe.plus.mapper.CourseMapper;
import com.xinghe.plus.mapper.EnrollmentMapper;
import com.xinghe.plus.mapper.StudentMapper;
import com.xinghe.util.MyBatisPlusUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentMapperTest {

    private SqlSession session;
    private EnrollmentMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisPlusUtil.getSqlSessionFactory().openSession();
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
        assertEquals(enrollment, mapper.selectById(enrollment.getId()), "零分有效，未传字段保持原值");
    }

    @Test
    void testDeleteById() {
        Enrollment enrollment = insertEnrollment();
        assertEquals(1, mapper.deleteById(enrollment.getId()));
        assertNull(mapper.selectById(enrollment.getId()));
    }

    @Test
    void testSelectPage() {
        Enrollment first = insertEnrollment();
        Integer studentId = first.getStudentId();
        String semester = first.getSemester();
        List<Enrollment> firstPageRecords = new ArrayList<>();
        firstPageRecords.add(first);
        for (int i = 0; i < 4; i++) {
            Enrollment enrollment = newEnrollment();
            enrollment.setStudentId(studentId);
            assertEquals(1, mapper.insert(enrollment));
            firstPageRecords.add(enrollment);
        }
        Enrollment last = newEnrollment();
        last.setStudentId(studentId);
        assertEquals(1, mapper.insert(last));

        Enrollment otherSemester = newEnrollment();
        otherSemester.setStudentId(studentId);
        otherSemester.setSemester("2026-2027-2");
        assertEquals(1, mapper.insert(otherSemester));
        insertEnrollment(); // 同学期的另一名学生不应进入分页结果。

        LambdaQueryWrapper<Enrollment> query = new LambdaQueryWrapper<Enrollment>()
                .eq(studentId != null, Enrollment::getStudentId, studentId)
                .eq(semester != null && !semester.isBlank(), Enrollment::getSemester, semester)
                .orderByAsc(Enrollment::getId);
        Page<Enrollment> firstPage = mapper.selectPage(new Page<>(1, 5), query);
        assertEquals(firstPageRecords, firstPage.getRecords());
        assertEquals(6, firstPage.getTotal());
        assertEquals(1, firstPage.getCurrent());
        assertEquals(5, firstPage.getSize());
        assertEquals(2, firstPage.getPages());

        Page<Enrollment> secondPage = mapper.selectPage(new Page<>(2, 5), query);
        assertEquals(List.of(last), secondPage.getRecords());
        assertEquals(6, secondPage.getTotal());
        assertEquals(2, secondPage.getCurrent());
        assertEquals(5, secondPage.getSize());
        assertEquals(2, secondPage.getPages());
    }

    private Enrollment insertEnrollment() {
        Enrollment enrollment = newEnrollment();
        assertEquals(1, mapper.insert(enrollment));
        return enrollment;
    }

    // 先创建学生和课程，满足外键；临时父表数据也一起回滚。
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
        enrollment.setSelectedAt(LocalDateTime.of(2026, 9, 2, 9, 10, 20));
        return enrollment;
    }
}
