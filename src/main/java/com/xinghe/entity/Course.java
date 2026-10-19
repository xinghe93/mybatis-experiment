package com.xinghe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("course")
public class Course {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("course_no")
    private String courseNo;
    private String name;
    private BigDecimal credit;
    private Short hours;
    private String teacher;

}
