package com.xinghe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("enrollment")
public class Enrollment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("student_id")
    private Integer studentId;
    @TableField("course_id")
    private Integer courseId;
    private String semester;
    @TableField("selected_at")
    private LocalDateTime selectedAt;
    private BigDecimal score;

}
