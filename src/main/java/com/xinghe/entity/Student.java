package com.xinghe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("student")
public class Student {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("student_no")
    private String studentNo;
    private String name;
    private String gender;
    private String major;
    private Short grade;
    @TableField("enrollment_date")
    private LocalDate enrollmentDate;

}
