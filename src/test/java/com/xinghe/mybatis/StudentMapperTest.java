package com.xinghe.mybatis;

import com.xinghe.entity.Student;
import com.xinghe.mybatis.mapper.StudentMapper;
import com.xinghe.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


public class StudentMapperTest {

    @Test
    void testInsertStudent() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession()) {
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);

            Student student = new Student();
            student.setStudentNo("20270001");
            student.setName("李明");
            student.setGender("男");
            student.setMajor("计算机科学与技术");
            student.setGrade(Short.valueOf("2027"));
            student.setEnrollmentDate(LocalDate.of(2027, 9, 1));

            int rows = studentMapper.insert(student);
            sqlSession.commit(); // 提交事务

            System.out.println("受影响行数：" + rows);
            System.out.println("新增学生ID：" + student.getId());
        }
    }

    @Test
    void testSelectById() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession()) {
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);

            Student student = studentMapper.selectById(1);

            assertNotNull(student);
            assertEquals(Integer.valueOf(1), student.getId());
            assertEquals("20230001", student.getStudentNo());
            assertEquals("张伟", student.getName());
            assertEquals(
                    LocalDate.of(2023, 9, 1),
                    student.getEnrollmentDate()
            );
        }
    }

    @Test
    void testDeleteById() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession()) {
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);

            int rows = studentMapper.deleteById(31);
            sqlSession.commit();

            assertEquals(1, rows);
            System.out.println("删除成功，受影响行数：" + rows);
        }
    }

}
