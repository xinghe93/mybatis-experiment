package com.xinghe.mybatis;

import com.xinghe.entity.Course;
import com.xinghe.entity.Enrollment;
import com.xinghe.entity.Student;
import com.xinghe.mybatis.mapper.CourseMapper;
import com.xinghe.mybatis.mapper.EnrollmentMapper;
import com.xinghe.mybatis.mapper.StudentMapper;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StudentMapperTest {

    private SqlSession sqlSession;
    private StudentMapper studentMapper;

    /**
     * 每个测试执行前打开独立会话，获取学生 Mapper。
     * 会话默认不自动提交，由测试结束后的清理方法统一回滚。
     */
    @BeforeEach
    void openSession() {
        sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
        studentMapper = sqlSession.getMapper(StudentMapper.class);
    }

    /**
     * 每个测试结束后回滚临时数据，并关闭会话释放数据库连接。
     * 即使测试断言失败，也会执行清理；即使回滚失败，也会尝试关闭会话。
     */
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

    /**
     * 验证新增学生影响 1 行、自增主键回填，并且查询结果与插入的字段一致。
     */
    @Test
    void testInsert() {
        Student student = newTestStudent();

        assertEquals(1, studentMapper.insert(student));
        assertNotNull(student.getId(), "新增后应回填主键");
        assertEquals(student, studentMapper.selectById(student.getId()));
    }

    @Test
    void testSelectById() {
        Student expected = insertTestStudent();

        Student actual = studentMapper.selectById(expected.getId());

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void testUpdateById() {
        Student student = insertTestStudent();

        Student update = new Student();
        update.setId(student.getId());
        update.setName("更新后的姓名");
        update.setMajor("软件工程");

        assertEquals(1, studentMapper.updateById(update));

        student.setName(update.getName());
        student.setMajor(update.getMajor());
    }

    /**
     * 验证没有选课记录的学生可以删除：影响行数为 1，删除后查询返回 null。
     */
    @Test
    void testDeleteByIdWithoutEnrollments() {
        Student student = insertTestStudent();

        assertEquals(1, studentMapper.deleteById(student.getId()));
        assertNull(studentMapper.selectById(student.getId()));
        System.out.println("【删除成功】影响行数为 1，学生已不存在");
    }

    /**
     * 验证删除不存在的学生返回 0，不抛出异常。
     * 先删除临时学生，再使用同一个编号重复删除，确保该编号确实不存在。
     */
    @Test
    void testDeleteByIdWhenStudentDoesNotExist() {
        Student student = insertTestStudent();
        assertEquals(1, studentMapper.deleteById(student.getId()));
        assertNull(studentMapper.selectById(student.getId()));

        // 用刚删除的 ID 验证不存在的情况，不猜测数据库中的编号。
        assertEquals(0, studentMapper.deleteById(student.getId()));
        assertNull(studentMapper.selectById(student.getId()));
        System.out.println("【学生不存在】影响行数为 0");
    }

    /**
     * 验证有选课记录的学生不能直接删除，MyBatis 会抛出 PersistenceException。
     * 检查底层完整性约束异常、SQLState 23000 和 MySQL 错误码 1451，
     * 确认失败原因是外键限制，并验证学生与选课记录仍然存在。
     *
     * @throws SQLException 查询保留的选课记录时发生 JDBC 错误
     */
    @Test
    void testDeleteByIdWithEnrollmentsThrowsException() throws SQLException {
        Student student = insertTestStudent();
        insertTestEnrollment(student.getId());

        PersistenceException exception = assertThrows(
                PersistenceException.class,
                () -> studentMapper.deleteById(student.getId())
        );
        SQLIntegrityConstraintViolationException cause = assertInstanceOf(
                SQLIntegrityConstraintViolationException.class, exception.getCause()
        );
        assertEquals("23000", cause.getSQLState());
        assertEquals(1451, cause.getErrorCode(), "应由外键拒绝删除父表记录");
        assertEquals(student, studentMapper.selectById(student.getId()),
                "删除失败后学生应仍然存在");

        try (PreparedStatement statement = sqlSession.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM enrollment WHERE student_id = ?")) {
            statement.setInt(1, student.getId());
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1), "删除失败后选课记录应仍然存在");
            }
        }
        System.out.println("【外键限制】删除被拒绝，错误码："
                + cause.getErrorCode() + "，学生和选课记录仍存在");
    }

    // 按姓名查询
    @Test
    void testSelectByCondition() {
        Student student = newTestStudent();
        student.setName(uniqueTestNumber());
        assertEquals(1, studentMapper.insert(student));

        Student condition = new Student();
        condition.setName(student.getName());
        List<Student> students = studentMapper.selectByCondition(condition);

        assertEquals(List.of(student), students);
    }

    // 根据多个 ID 查询
    @Test
    void testSelectByIds() {
        Student first = insertTestStudent();
        Student second = insertTestStudent();

        List<Student> students = studentMapper.selectByIds(
                List.of(first.getId(), second.getId()));

        assertEquals(List.of(first, second), students);
    }

    // 统计指定专业的学生人数
    @Test
    void testCountByCondition() {
        Student student = newTestStudent();
        student.setMajor(uniqueTestNumber());
        assertEquals(1, studentMapper.insert(student));

        Student condition = new Student();
        condition.setMajor(student.getMajor());
        int count = studentMapper.countByCondition(condition);

        assertEquals(1, count);
    }

    // 查询第一页，每页两条，按 ID 升序排列
    @Test
    void testSelectPage() {
        insertTestStudent();
        insertTestStudent();

        List<Student> students = studentMapper.selectPage(0, 2);

        assertEquals(2, students.size());
        assertTrue(students.get(0).getId() < students.get(1).getId());
    }

    /**
     * 构造字段完整的临时学生对象，仅创建对象，不写入数据库。
     *
     * @return 尚未分配主键、使用随机学号的学生对象
     */
    private Student newTestStudent() {
        Student student = new Student();
        student.setStudentNo(uniqueTestNumber());
        student.setName("测试学生");
        student.setGender("男");
        student.setMajor("计算机科学与技术");
        student.setGrade((short) 2026);
        student.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        return student;
    }

    /**
     * 在当前测试会话中插入临时学生，并检查插入行数与主键回填。
     *
     * @return 已回填数据库主键的学生对象
     */
    private Student insertTestStudent() {
        Student student = newTestStudent();
        assertEquals(1, studentMapper.insert(student));
        assertNotNull(student.getId());
        return student;
    }

    /**
     * 生成带 test 前缀的随机编号，供临时学生学号和课程代码使用。
     *
     * @return 长度为 20 个字符的随机编号
     */
    private String uniqueTestNumber() {
        // 学号与课程代码最多 20 个字符，随机值避免重复执行产生唯一键冲突。
        return "test" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 为指定的临时学生创建一门临时课程和一条选课记录，用于验证外键删除限制。
     * Mapper 使用当前测试会话，数据会随该会话的事务一起回滚。
     *
     * @param studentId 当前事务中已存在的学生编号
     * @throws PersistenceException 插入测试课程或选课记录时发生数据库错误
     */
    private void insertTestEnrollment(Integer studentId) {
        CourseMapper courseMapper = sqlSession.getMapper(CourseMapper.class);
        Course course = new Course();
        course.setCourseNo(uniqueTestNumber());
        course.setName("测试课程");
        course.setCredit(new BigDecimal("2.0"));
        course.setHours((short) 32);
        course.setTeacher("测试教师");
        courseMapper.insert(course);
        assertNotNull(course.getId(), "新增课程后应回填主键");

        EnrollmentMapper enrollmentMapper = sqlSession.getMapper(EnrollmentMapper.class);
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(course.getId());
        enrollment.setSemester("2026-2027-1");
        enrollment.setSelectedAt(LocalDateTime.of(2026, 9, 2, 9, 0));
        assertEquals(1, enrollmentMapper.insert(enrollment));
        assertNotNull(enrollment.getId(), "新增选课记录后应回填主键");
    }

}
