package com.xinghe.plus;

import com.xinghe.entity.Student;
import com.xinghe.plus.mapper.StudentMapper;
import com.xinghe.support.TestDataCleaner;
import com.xinghe.util.MyBatisPlusUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentMapperTest {

    private final TestDataCleaner testData = new TestDataCleaner();
    private SqlSession session;
    private StudentMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisPlusUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(StudentMapper.class);
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
        Student student = newStudent();
        assertEquals(1, mapper.insert(student));
        session.commit();
        assertNotNull(student.getId());
        assertEquals(student, mapper.selectById(student.getId()));
    }

    @Test
    void testSelectById() {
        Student student = insertStudent();
        assertEquals(student, mapper.selectById(student.getId()));
    }

    @Test
    void testUpdateById() {
        Student student = insertStudent();
        Student update = new Student();
        update.setId(student.getId());
        update.setName("新姓名");
        assertEquals(1, mapper.updateById(update));
        session.commit();
        student.setName(update.getName());
        assertEquals(student, mapper.selectById(student.getId()), "未传字段保持原值");
    }

    @Test
    void testDeleteById() {
        Student student = insertStudent();
        assertEquals(1, mapper.deleteById(student.getId()));
        session.commit();
        assertNull(mapper.selectById(student.getId()));
    }

    private Student insertStudent() {
        Student student = newStudent();
        assertEquals(1, mapper.insert(student));
        session.commit();
        return student;
    }

    private Student newStudent() {
        Student student = new Student();
        testData.track(student);
        student.setStudentNo(UUID.randomUUID().toString().substring(0, 20));
        student.setName("测试学生");
        student.setGender("女");
        student.setMajor("软件工程");
        student.setGrade((short) 2026);
        student.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        return student;
    }
}
