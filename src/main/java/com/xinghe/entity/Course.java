package com.xinghe.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Course {

    private Integer id;
    private String courseNo;
    private String name;
    private BigDecimal credit;
    private Short hours;
    private String teacher;

}
