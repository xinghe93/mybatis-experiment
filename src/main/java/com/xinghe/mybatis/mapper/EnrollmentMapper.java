package com.xinghe.mybatis.mapper;

import com.xinghe.entity.Enrollment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EnrollmentMapper {

    /**
     * 调用前校验必填字段，成绩可为空，非空时须在 0～100 之间。
     */
    int insert(Enrollment enrollment);

    Enrollment selectById(Long id);

    /**
     * 调用前校验 ID 和至少一个可更新字段；成绩 null 跳过、0 有效，外键不更新。
     */
    int updateById(Enrollment enrollment);

    int deleteById(Long id);

    /**
     * 调用前校验列表非空且无 null 元素，每条记录的学生、课程、学期、选课时间必填，
     * 成绩为 null 或 0～100；单条多值 INSERT，不要求回填主键，事务由调用方控制。
     */
    int insertBatch(@Param("list") List<Enrollment> enrollments);

    /**
     * 调用前校验学生 ID、学期必填；二者以 AND 精确匹配。
     */
    List<Enrollment> selectByStudentAndSemester(@Param("studentId") Integer studentId,
                                                @Param("semester") String semester);

    /**
     * null 或空集合安全地删除零行。
     */
    int deleteBatch(@Param("ids") List<Long> ids);

}
