package com.xinghe.mybatis;

import com.xinghe.entity.Student;
import com.xinghe.mybatis.mapper.StudentMapper;
import com.xinghe.support.TestDataCleaner;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentMapperTest {

    private final TestDataCleaner testData = new TestDataCleaner();
    private SqlSession session;
    private StudentMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(StudentMapper.class);
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
        Student student = newStudent("测试学生", "软件工程", (short) 2026);
        assertEquals(1, mapper.insert(student));
        session.commit();
        assertNotNull(student.getId());
        assertEquals(student, mapper.selectById(student.getId()));
    }

    @Test
    void testSelectById() {
        Student student = insertStudent("测试学生", "软件工程", (short) 2026);
        assertEquals(student, mapper.selectById(student.getId()));
    }

    @Test
    void testUpdateById() {
        Student student = insertStudent("测试学生", "软件工程", (short) 2026);
        Student update = new Student();
        update.setId(student.getId());
        update.setName("新姓名");
        update.setMajor("计算机科学与技术");
        assertEquals(1, mapper.updateById(update));
        session.commit();
        student.setName(update.getName());
        student.setMajor(update.getMajor());
        assertEquals(student, mapper.selectById(student.getId()), "未传字段保持原值");
    }

    @Test
    void testDeleteById() {
        Student student = insertStudent("测试学生", "软件工程", (short) 2026);
        assertEquals(1, mapper.deleteById(student.getId()));
        session.commit();
        assertNull(mapper.selectById(student.getId()));
    }

    @Test
    void testSelectByCondition() {
        String name = UUID.randomUUID().toString().substring(0, 8);
        Student expected = insertStudent("张" + name, "计算机科学与技术", (short) 2026);
        insertStudent("张" + name, "软件工程", (short) 2026);
        insertStudent("张" + name, "计算机科学与技术", (short) 2025);

        Student condition = new Student();
        condition.setName("  " + name + "  ");
        condition.setMajor("计算机");
        condition.setGrade((short) 2026);
        assertEquals(List.of(expected), mapper.selectByCondition(condition));
        assertTrue(mapper.selectByCondition(null).contains(expected));
    }

    @Test
    void testSelectByConditionWithTrim() {
        String token = UUID.randomUUID().toString().substring(0, 8);
        String major = "计算机" + token;
        Student first = insertStudent("张" + token, major, (short) 2026);
        insertStudent("张" + token, "软件工程", (short) 2026);
        Student third = insertStudent("张" + token, major, (short) 2025);

        // 组合条件只匹配第一条记录。
        Student condition = new Student();
        condition.setName("  " + token + "  ");
        condition.setMajor(major);
        condition.setGrade((short) 2026);
        assertEquals(List.of(first), mapper.selectByConditionWithTrim(condition));

        // 只按专业查询，验证 trim 能去掉开头的 AND。
        Student majorOnly = new Student();
        majorOnly.setMajor(major);
        assertEquals(List.of(first, third), mapper.selectByConditionWithTrim(majorOnly));

        // 空白与 null 条件都应查询全部，和 where 版本一致。
        Student blank = new Student();
        blank.setName("  ");
        blank.setMajor("  ");
        List<Student> all = mapper.selectByCondition(null);
        assertEquals(all, mapper.selectByConditionWithTrim(blank));
        assertEquals(all, mapper.selectByConditionWithTrim(null));

        condition.setName("不存在的学生" + token);
        assertTrue(mapper.selectByConditionWithTrim(condition).isEmpty());
    }

    private Student insertStudent(String name, String major, short grade) {
        Student student = newStudent(name, major, grade);
        assertEquals(1, mapper.insert(student));
        session.commit();
        return student;
    }

    private Student newStudent(String name, String major, short grade) {
        Student student = new Student();
        testData.track(student);
        student.setStudentNo(UUID.randomUUID().toString().substring(0, 20));
        student.setName(name);
        student.setGender("男");
        student.setMajor(major);
        student.setGrade(grade);
        student.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        return student;
    }
}
