package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.exceptions.PersistenceException;

import java.util.List;

public interface StudentMapper {

    int insert(Student student);

    Student selectById(Integer id);

    /**
     * 调用前校验 ID 非空，且至少提供一个可更新字段；学号不参与更新。
     */
    int updateById(Student student);

    /**
     * 按 ID 删除学生，关联选课由数据库外键限制。
     *
     * @throws PersistenceException 存在选课记录等数据库约束导致删除失败
     */
    int deleteById(Integer id);

    /**
     * 姓名、专业模糊匹配，年级精确匹配；null 或空白条件不参与查询。
     */
    List<Student> selectByCondition(@Param("condition") Student student);

    /**
     * 与 selectByCondition 条件相同，使用 trim 演示 where 标签的等效写法。
     */
    List<Student> selectByConditionWithTrim(@Param("condition") Student student);

}
