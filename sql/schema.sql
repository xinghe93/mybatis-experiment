-- MyBatis 学生选课实验：适用于 MySQL 8.0.16+。
-- 仅创建缺失的表，不删除已有表或数据。
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS mybatis_experiment
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mybatis_experiment;

CREATE TABLE IF NOT EXISTS student (
    id INT NOT NULL AUTO_INCREMENT COMMENT '学生编号',
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(6) NOT NULL DEFAULT '未填写' COMMENT '性别',
    major VARCHAR(100) NOT NULL COMMENT '专业',
    grade SMALLINT NOT NULL COMMENT '入学年级，例如2026级',
    enrollment_date DATE NOT NULL COMMENT '入学日期',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_student_major_grade (major, grade),
    CONSTRAINT chk_student_gender CHECK (gender IN ('男', '女', '未填写')),
    CONSTRAINT chk_student_grade CHECK (grade BETWEEN 2000 AND 9999)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生表';

CREATE TABLE IF NOT EXISTS course (
    id INT NOT NULL AUTO_INCREMENT COMMENT '课程编号',
    course_no VARCHAR(20) NOT NULL COMMENT '课程代码',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit DECIMAL(3, 1) NOT NULL COMMENT '学分',
    hours SMALLINT NOT NULL COMMENT '学时',
    teacher VARCHAR(50) NOT NULL COMMENT '授课教师',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_no (course_no),
    KEY idx_course_teacher (teacher),
    CONSTRAINT chk_course_credit CHECK (credit > 0 AND credit <= 10),
    CONSTRAINT chk_course_hours CHECK (hours > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程表';

CREATE TABLE IF NOT EXISTS enrollment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '选课记录编号',
    student_id INT NOT NULL COMMENT '学生编号',
    course_id INT NOT NULL COMMENT '课程编号',
    semester VARCHAR(11) NOT NULL COMMENT '学期，例如2026-2027-1',
    selected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    score DECIMAL(5, 2) NULL COMMENT '成绩，尚未评分时为空',
    PRIMARY KEY (id),
    UNIQUE KEY uk_enrollment_student_course_semester (student_id, course_id, semester),
    KEY idx_enrollment_student_semester (student_id, semester),
    -- 外键所用索引仅包含非空字段，成绩仍可为空。
    KEY idx_enrollment_course_semester (course_id, semester),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES student (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_enrollment_score CHECK (score IS NULL OR score BETWEEN 0 AND 100)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '选课记录表';
