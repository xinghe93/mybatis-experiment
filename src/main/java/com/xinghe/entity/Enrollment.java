package com.xinghe.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Enrollment {

    private Long id;
    private Integer studentId;
    private Integer courseId;
    private String semester;
    private LocalDateTime selectedAt;
    private BigDecimal score;

}
