package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Student;
import org.apache.ibatis.exceptions.PersistenceException;

import java.util.List;

public interface StudentMapper {

    int insert(Student student);

    Student selectById(Integer id);

    int updateById(Student student);

    /**
     * 按编号删除学生，不删除关联的选课记录。
     *
     * @param id 学生编号
     * @return 删除成功返回 1；学生不存在返回 0
     * @throws PersistenceException 数据库操作失败时抛出；有选课记录时，
     *                              外键约束会拒绝删除，底层原因是 SQL 完整性约束异常
     */
    int deleteById(Integer id);

    List<Student> selectByCondition();

    List<Student> selectByIds();

    int countByCondition(Student student);



}
