package com.xinghe.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Student {

    private Integer id;
    private String studentNo;
    private String name;
    private String gender;
    private String major;
    private Short grade;
    private LocalDate enrollmentDate;

}
