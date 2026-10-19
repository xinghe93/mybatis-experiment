package com.xinghe.support;

import com.xinghe.entity.Course;
import com.xinghe.entity.Student;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 记录本次测试创建的学生、课程，清理时先删除关联选课，再删除父表记录。 */
public class TestDataCleaner {

    private final List<Student> students = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();

    public void track(Student student) {
        students.add(student);
    }

    public void track(Course course) {
        courses.add(course);
    }

    public void clean(SqlSessionFactory factory) throws SQLException {
        try (SqlSession session = factory.openSession()) {
            try {
                Connection connection = session.getConnection();
                for (Course course : courses) {
                    delete(connection, "DELETE FROM enrollment WHERE course_id = ?", course.getId());
                }
                for (Student student : students) {
                    delete(connection, "DELETE FROM enrollment WHERE student_id = ?", student.getId());
                }
                for (Course course : courses) {
                    delete(connection, "DELETE FROM course WHERE id = ?", course.getId());
                }
                for (Student student : students) {
                    delete(connection, "DELETE FROM student WHERE id = ?", student.getId());
                }
                // 清理直接使用 JDBC，强制提交，让 SqlSession 提交这些删除操作。
                session.commit(true);
            } catch (SQLException | RuntimeException e) {
                session.rollback(true);
                throw e;
            }
        }
    }

    private void delete(Connection connection, String sql, Integer id) throws SQLException {
        if (id != null) {
            try (var statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        }
    }
}
