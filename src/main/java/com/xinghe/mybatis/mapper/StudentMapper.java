package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Student;
import org.apache.ibatis.annotations.Param;

public interface StudentMapper {

    Student selectById(Integer id);

}
