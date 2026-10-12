package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Student;

import java.util.List;

public interface StudentMapper {

    int insert(Student student);

    Student selectById(Integer id);

    int updateById(Student student);

    int deleteById(Integer id);

    List<Student> selectByCondition();

    List<Student> selectByIds();

    int countByCondition(Student student);



}
