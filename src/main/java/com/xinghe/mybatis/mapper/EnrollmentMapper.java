package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Enrollment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EnrollmentMapper {

    int insert(Enrollment enrollment);

    Enrollment selectById(Long id);

    List<Enrollment> selectAll();

    int updateById(Enrollment enrollment);

    int deleteById(Long id);

    List<Enrollment> selectByCondition(Enrollment enrollment);

    List<Enrollment> selectByIds(List<Long> ids);

    int countByCondition(Enrollment enrollment);

    List<Enrollment> selectPage(@Param("offset") int offset,
                                @Param("pageSize") int pageSize);

}
