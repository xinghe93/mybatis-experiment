-- 导入后的只读检查：初始数据为30名学生、8门课程、60条选课记录。
SET NAMES utf8mb4;
USE mybatis_experiment;

SELECT 'student' AS table_name, COUNT(*) AS row_count FROM student
UNION ALL
SELECT 'course', COUNT(*) FROM course
UNION ALL
SELECT 'enrollment', COUNT(*) FROM enrollment;

-- 课程、学期选课人数与平均分；AVG忽略NULL，未评分课程平均分为空。
-- LEFT JOIN保留没有选课记录的课程（初始数据为课程8）。
SELECT c.id, c.name AS course_name, e.semester, COUNT(e.id) AS enrollment_count,
       COUNT(e.score) AS graded_count, ROUND(AVG(e.score), 2) AS average_score
FROM course c
LEFT JOIN enrollment e ON e.course_id = c.id
GROUP BY c.id, c.name, e.semester
ORDER BY c.id, e.semester;

-- 三表关联查询；同名字段使用别名，避免结果映射混淆。
SELECT e.id, s.student_no, s.name AS student_name, s.major, s.grade,
       c.course_no, c.name AS course_name, c.teacher, c.credit,
       e.semester, e.selected_at, e.score
FROM enrollment e
JOIN student s ON s.id = e.student_id
JOIN course c ON c.id = e.course_id
ORDER BY e.semester, s.id, c.id;

-- 没有选课记录的学生；初始数据为学生21～30，共10人。
SELECT s.id, s.student_no, s.name
FROM student s
WHERE NOT EXISTS (SELECT 1 FROM enrollment e WHERE e.student_id = s.id)
ORDER BY s.id;

-- 统计已评分、未评分和不及格记录；初始数据分别为20、40、3。
SELECT COUNT(score) AS graded_count,
       SUM(score IS NULL) AS ungraded_count,
       SUM(score < 60) AS failed_count
FROM enrollment;

-- 重复选课检查；预期0行。
SELECT student_id, course_id, semester, COUNT(*) AS duplicate_count
FROM enrollment
GROUP BY student_id, course_id, semester
HAVING COUNT(*) > 1;

-- 异常成绩或选课早于入学的记录；预期0行。
SELECT e.id, e.student_id, e.score, e.selected_at, s.enrollment_date
FROM enrollment e
JOIN student s ON s.id = e.student_id
WHERE e.score < 0 OR e.score > 100 OR e.selected_at < s.enrollment_date;
