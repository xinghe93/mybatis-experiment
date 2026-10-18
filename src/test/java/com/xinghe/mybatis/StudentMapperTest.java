package com.xinghe.mybatis;

import com.xinghe.entity.Student;
import com.xinghe.mybatis.mapper.StudentMapper;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentMapperTest {

    private SqlSession session;
    private StudentMapper mapper;

    @BeforeEach
    void setUp() {
        session = MyBatisUtil.getSqlSessionFactory().openSession();
        mapper = session.getMapper(StudentMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            try {
                session.rollback(); // 临时数据随事务回滚，不影响实验库。
            } finally {
                session.close();
            }
        }
    }

    @Test
    void testInsert() {
        Student student = newStudent("测试学生", "软件工程", (short) 2026);
        assertEquals(1, mapper.insert(student));
        assertNotNull(student.getId());
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
        student.setName(update.getName());
        student.setMajor(update.getMajor());
        assertEquals(student, mapper.selectById(student.getId()), "未传字段保持原值");
    }

    @Test
    void testDeleteById() {
        Student student = insertStudent("测试学生", "软件工程", (short) 2026);
        assertEquals(1, mapper.deleteById(student.getId()));
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

    private Student insertStudent(String name, String major, short grade) {
        Student student = newStudent(name, major, grade);
        mapper.insert(student);
        return student;
    }

    private Student newStudent(String name, String major, short grade) {
        Student student = new Student();
        student.setStudentNo(UUID.randomUUID().toString().substring(0, 20));
        student.setName(name);
        student.setGender("男");
        student.setMajor(major);
        student.setGrade(grade);
        student.setEnrollmentDate(LocalDate.of(2026, 9, 1));
        return student;
    }
}
