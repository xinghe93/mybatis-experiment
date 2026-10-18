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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnrollmentMapperTest {

    private SqlSession sqlSession;
    private EnrollmentMapper enrollmentMapper;

    @BeforeEach
    void openSession() {
        sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
        enrollmentMapper = sqlSession.getMapper(EnrollmentMapper.class);
    }

    @AfterEach
    void rollbackAndCloseSession() {
        if (sqlSession != null) {
            try {
                sqlSession.rollback();
            } finally {
                sqlSession.close();
            }
        }
    }

    // 新增选课记录，并检查主键回填。
    @Test
    void testInsert() {
        Enrollment enrollment = newTestEnrollment();

        assertEquals(1, enrollmentMapper.insert(enrollment));
        assertNotNull(enrollment.getId());
    }

    // 根据 ID 查询，检查所有字段是否正确映射。
    @Test
    void testSelectById() {
        Enrollment enrollment = newTestEnrollment();
        enrollment.setScore(new BigDecimal("0.00"));
        assertEquals(1, enrollmentMapper.insert(enrollment));

        assertEquals(enrollment, enrollmentMapper.selectById(enrollment.getId()));
    }

    // 查询全部选课记录。
    @Test
    void testSelectAll() {
        Enrollment enrollment = insertTestEnrollment();

        assertTrue(enrollmentMapper.selectAll().contains(enrollment));
    }

    // 只更新成绩，零分是有效成绩，其他字段应保持原值。
    @Test
    void testUpdateById() {
        Enrollment enrollment = insertTestEnrollment();
        Enrollment update = new Enrollment();
        update.setId(enrollment.getId());
        update.setScore(new BigDecimal("0.00"));

        assertEquals(1, enrollmentMapper.updateById(update));
        enrollment.setScore(update.getScore());
        assertEquals(enrollment, enrollmentMapper.selectById(enrollment.getId()));
    }

    // 删除后应查询不到。
    @Test
    void testDeleteById() {
        Enrollment enrollment = insertTestEnrollment();

        assertEquals(1, enrollmentMapper.deleteById(enrollment.getId()));
        assertNull(enrollmentMapper.selectById(enrollment.getId()));
    }

    // 按学生、课程、学期和成绩查询。
    @Test
    void testSelectByCondition() {
        Enrollment enrollment = newTestEnrollment();
        enrollment.setScore(new BigDecimal("0.00"));
        assertEquals(1, enrollmentMapper.insert(enrollment));
        Enrollment condition = new Enrollment();
        condition.setStudentId(enrollment.getStudentId());
        condition.setCourseId(enrollment.getCourseId());
        condition.setSemester(enrollment.getSemester());
        condition.setScore(enrollment.getScore());

        assertEquals(List.of(enrollment), enrollmentMapper.selectByCondition(condition));
    }

    // 批量查询按 ID 升序返回，空集合或 null 返回空列表。
    @Test
    void testSelectByIds() {
        Enrollment first = insertTestEnrollment();
        Enrollment second = insertTestEnrollment();

        assertEquals(List.of(first, second), enrollmentMapper.selectByIds(
                List.of(second.getId(), first.getId())));
        assertTrue(enrollmentMapper.selectByIds(List.of()).isEmpty());
        assertTrue(enrollmentMapper.selectByIds(null).isEmpty());
    }

    // 统计指定学生的选课数量。
    @Test
    void testCountByCondition() {
        Enrollment enrollment = insertTestEnrollment();
        Enrollment condition = new Enrollment();
        condition.setStudentId(enrollment.getStudentId());

        assertEquals(1, enrollmentMapper.countByCondition(condition));
    }

    // 查询第二页，每页两条。
    @Test
    void testSelectPage() {
        insertTestEnrollment();
        insertTestEnrollment();
        insertTestEnrollment();
        List<Enrollment> all = enrollmentMapper.selectAll();

        assertEquals(all.subList(2, Math.min(4, all.size())),
                enrollmentMapper.selectPage(2, 2));
    }

    private Enrollment insertTestEnrollment() {
        Enrollment enrollment = newTestEnrollment();
        assertEquals(1, enrollmentMapper.insert(enrollment));
        assertNotNull(enrollment.getId());
        return enrollment;
    }

    /** 先创建学生和课程，满足选课记录的外键约束；所有数据都会一起回滚。 */
    private Enrollment newTestEnrollment() {
        String number = "test" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Student student = new Student();
        student.setStudentNo(number);
        student.setName("测试学生");
        student.setGender("男");
        student.setMajor("软件工程");
        student.setGrade((short) 2026);
        student.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        assertEquals(1, sqlSession.getMapper(StudentMapper.class).insert(student));

        Course course = new Course();
        course.setCourseNo(number);
        course.setName("测试课程");
        course.setCredit(new BigDecimal("2.0"));
        course.setHours((short) 32);
        course.setTeacher("测试教师");
        assertEquals(1, sqlSession.getMapper(CourseMapper.class).insert(course));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setSemester("2026-2027-1");
        enrollment.setSelectedAt(LocalDateTime.of(2026, 9, 2, 9, 0));
        return enrollment;
    }
}
