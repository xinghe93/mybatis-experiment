# MyBatis 与 MyBatis-Plus Mapper 实验计划

**目标：** 学生用于信息管理，课程用于查找课程，选课记录用于批量选课、成绩登记和记录查询。

**范围：** 保持现有项目框架和三张表不变；原生代码放在 `com.xinghe.mybatis.mapper`，Plus 代码放在 `com.xinghe.plus.mapper`。本次仅实施原生 MyBatis Mapper、对应 XML / 注解和测试；Plus 实现暂不改动。

## 一、实验要求如何分配

| 技术 | 实验要求 | 主要落点 |
| --- | --- | --- |
| MyBatis | 多条件模糊查询：`if / where` | StudentMapper：按姓名、专业、年级筛选学生 |
| MyBatis | 动态更新：`set` | StudentMapper：只修改传入的学生信息 |
| MyBatis | 批量插入：`foreach` | EnrollmentMapper：一次为学生添加多条选课记录 |
| MyBatis | 批量删除：`foreach` | EnrollmentMapper：按选课记录 ID 批量删除 |
| MyBatis | 分支选择：`choose / when / otherwise` | CourseMapper：优先按课程代码，其次名称、教师查找课程 |
| MyBatis-Plus | 实体类注解映射 | Student、Course、Enrollment 三个共用实体 |
| MyBatis-Plus | BaseMapper 零手写 SQL 的通用 CRUD | 三个 Mapper 均具备并验证基础增删改查 |
| MyBatis-Plus | 条件构造器多条件查询 | CourseMapper：按名称、教师和学分筛选课程 |
| MyBatis-Plus | 分页插件与分页查询 | EnrollmentMapper：按学生、学期分页查看选课记录 |

## 二、原生 MyBatis 的 Mapper 设计

每个接口都包含 `insert、selectById、updateById、deleteById`，扩展查询和批量操作按业务分配。

### 1. StudentMapper：学生信息维护，共 5 个方法

接口：`src/main/java/com/xinghe/mybatis/mapper/StudentMapper.java`。

SQL：`src/main/resources/com/xinghe/mybatis/mapper/StudentMapper.xml`。

| 方法签名 | 功能 | 实现重点 |
| --- | --- | --- |
| `int insert(Student student)` | 新增学生 | 插入学生信息，回填自增主键 |
| `Student selectById(Integer id)` | 查看学生信息 | 按主键查询，供信息展示和更新结果验证使用 |
| `int updateById(Student student)` | 修改学生信息 | `set / if` 按需更新姓名、性别、专业、年级、入学日期，WHERE 固定按 id |
| `int deleteById(Integer id)` | 删除学生 | 按主键删除；存在选课记录时由外键拒绝 |
| `List<Student> selectByCondition(@Param("condition") Student condition)` | 筛选学生 | 姓名、专业模糊匹配；年级精确匹配；`if / where` 组合条件 |

组合查询示例：姓名包含“张”、专业包含“计算机”、年级为 2026，同时提供时三个条件按 AND 组合。空条件查询全部，列表按 id 升序。

更新时学号不参与修改，null 字段保持原值；调用前要求提供 id 和至少一个有效更新字段，避免生成空 SET。

### 2. CourseMapper：课程维护与查找，共 5 个方法

接口与 SQL：`src/main/java/com/xinghe/mybatis/mapper/CourseMapper.java`，全部使用注解，不创建 XML。

| 方法签名 | 功能 | 实现重点 |
| --- | --- | --- |
| `int insert(Course course)` | 添加课程 | `@Insert`；通过 `@Options` 回填自增主键 |
| `Course selectById(Integer id)` | 查看课程信息 | `@Select`，按主键查询课程详情 |
| `int updateById(Course course)` | 修改课程信息 | `@Update` 固定更新名称、学分、学时、教师，WHERE 按 id；调用前提供这四个字段，课程代码不变 |
| `int deleteById(Integer id)` | 删除课程 | `@Delete` 按主键删除；存在选课记录时由外键拒绝 |
| `List<Course> selectByPriority(@Param("condition") Course condition)` | 按优先条件查找课程 | `@Select` 的 `<script>` 中使用 `choose / when / otherwise` |

分支规则：

1. 提供课程代码：按 `courseNo` 精确查询。
2. 否则提供课程名称：按 `name` 模糊查询。
3. 否则提供教师：按 `teacher` 模糊查询。
4. 否则：查询全部课程，按 id 升序。

每次只选第一个有效分支，不叠加其他条件。高优先条件没有匹配结果时返回空列表，不继续尝试后面的分支。该接口用于演示“优先查找”；多条件课程筛选交给 Plus 的 Wrapper 演示。

### 3. EnrollmentMapper：选课记录与成绩，共 7 个方法

接口：`src/main/java/com/xinghe/mybatis/mapper/EnrollmentMapper.java`。

SQL：`src/main/resources/com/xinghe/mybatis/mapper/EnrollmentMapper.xml`。

| 方法签名 | 功能 | 实现重点 |
| --- | --- | --- |
| `int insert(Enrollment enrollment)` | 单条选课 | 插入一条选课记录，回填自增主键 |
| `Enrollment selectById(Long id)` | 查看选课记录 | 按主键查询 |
| `int updateById(Enrollment enrollment)` | 修改选课记录或登记成绩 | `set / if` 按非 null 字段更新学期、选课时间、成绩，WHERE 按 id；不修改学生、课程 ID |
| `int deleteById(Long id)` | 删除单条选课记录 | 按主键删除 |
| `int insertBatch(@Param("list") List<Enrollment> list)` | 批量选课 | `foreach` 生成一条多行 VALUES 插入语句 |
| `List<Enrollment> selectByStudentAndSemester(@Param("studentId") Integer studentId, @Param("semester") String semester)` | 查看某学生某学期的选课记录 | 学生 ID、学期均为必填，精确匹配，按 id 升序 |
| `int deleteBatch(@Param("ids") List<Long> ids)` | 批量删除选课记录 | `foreach` 生成 `WHERE id IN (...)` |

选课记录主键使用 `Long`，学生、课程外键使用 `Integer`。成绩登记直接复用 `updateById`，只传 id 和 score，不再增加独立的成绩更新方法。

批量插入前准备好学生 ID、课程 ID、学期和选课时间，成绩可为 null。通过学生与学期查询确认新增记录，再获取主键进行成绩登记或批量删除，不要求批量主键回填。

`updateById` 要求 id 非 null、至少一个有效更新字段；传入成绩时必须在 0～100 之间，0 分有效，null 表示不修改。修改学期仍受组合唯一约束限制。本实验不增加清空成绩、换课等功能。批量删除定位管理指定的记录，不额外设计退课业务规则。

## 三、MyBatis-Plus 的 Mapper 设计

### 1. 三个接口均继承 BaseMapper，具备基础 CRUD

| 接口文件 | 定义 | 基础功能与扩展示例 |
| --- | --- | --- |
| `src/main/java/com/xinghe/plus/mapper/StudentMapper.java` | `extends BaseMapper<Student>` | 用 `insert、selectById、updateById、deleteById` 完成学生 CRUD |
| `src/main/java/com/xinghe/plus/mapper/CourseMapper.java` | `extends BaseMapper<Course>` | 基础 CRUD；另用 `selectList(wrapper)` 实现课程多条件查询 |
| `src/main/java/com/xinghe/plus/mapper/EnrollmentMapper.java` | `extends BaseMapper<Enrollment>` | 基础 CRUD；另用 `selectPage(page, wrapper)` 分页查看选课记录 |

三个接口均通过继承获得 `insert、selectById、updateById、deleteById`，每张表都验证基础 CRUD。接口体可以为空，无需重复声明方法；条件查询和分页按上表分工，不添加包装 BaseMapper 调用的自定义方法。

**课程筛选：** 在测试或调用处构造 `LambdaQueryWrapper<Course>`，组合课程名称 `like`、教师 `like`、最低学分 `ge`，按 id 升序。条件为空时不加入对应限制。

**选课分页：** 构造 `LambdaQueryWrapper<Enrollment>`，按学生 ID 和学期精确筛选，按 id 升序；调用 `selectPage(new Page<>(1, 5), wrapper)`，查看记录列表、总数与总页数，并演示第 2 页。两个条件均可选，以便同时查看全体或某学生的选课记录。

### 2. 实体注解映射

沿用 `com.xinghe.entity` 下的三个实体，后续只补充必要注解，不复制两套实体。

| 实体 | 表映射 | 主键映射 | 显式字段映射 |
| --- | --- | --- | --- |
| Student | `@TableName("student")` | `@TableId(value = "id", type = IdType.AUTO)` | `@TableField` 映射 `studentNo → student_no`、`enrollmentDate → enrollment_date` |
| Course | `@TableName("course")` | 同上 | `@TableField` 映射 `courseNo → course_no` |
| Enrollment | `@TableName("enrollment")` | 同上 | `@TableField` 映射 `studentId → student_id`、`courseId → course_id`、`selectedAt → selected_at` |

其余同名字段默认映射。参考：[实体注解](https://baomidou.com/reference/annotation/)、[BaseMapper 通用方法](https://baomidou.com/guides/data-interface/)。

### 3. 分页的最小配套配置

- Plus 会话工厂使用 `MybatisSqlSessionFactoryBuilder`，注册 `com.xinghe.plus.mapper`；原生会话工厂注册 `com.xinghe.mybatis.mapper`，避免两套同名 Mapper 混用。
- 在 Plus 会话工厂注册 `MybatisPlusInterceptor`，添加 `PaginationInnerInterceptor(DbType.MYSQL)`，只配置一次。
- 沿用现有 Plus 核心、扩展及 `mybatis-plus-jsqlparser` 依赖；不更换项目框架、不引入 Spring Boot。
- 确认日志生成数据库分页 SQL，并正确返回 `records、total、current、size、pages`。参考：[分页插件](https://baomidou.com/plugins/pagination/)。

以上实体注解与分页配置仅列入后续 Plus 配套实施范围，本次不修改。

## 四、统一约定与后续验收

- 查询字符串先去除首尾空白；null、空串、纯空白视为未提供。模糊匹配使用 `LIKE CONCAT('%', #{...}, '%')`，不直接拼接用户输入。
- 写操作返回影响行数；单条查询未找到返回 null，列表查询未找到返回空列表。
- 批量插入要求集合非空、元素非 null、必填字段完整。学生、课程须已存在；同一学生、课程、学期不能重复选课，操作失败时回滚。
- 批量删除的 null 或空集合必须在 SQL 中回退为 `WHERE 1 = 0`，影响 0 行，防止误删全表。
- 删除被选课记录引用的学生或课程受外键限制；清理测试数据时先删选课记录，再删学生、课程。
- 查询直接返回现有实体，不新增查询 DTO、关联结果类、联查统计或 Service。测试使用临时数据，写操作结束后回滚。

### 后续实施清单

- [x] StudentMapper 的 XML 实现 5 个方法，验证基础 CRUD、组合模糊查询、空条件、部分更新和未传字段保持原值。
- [x] CourseMapper 的注解实现 5 个方法，验证基础 CRUD、三个 when、otherwise，以及多条件同时提供时仅命中最高优先级分支。
- [x] EnrollmentMapper 的 XML 实现 7 个方法，验证基础 CRUD、批量选课、按学生学期查询、0 分登记、批量删除及空集合保护。
- [ ] 为三个实体补充 Plus 注解，建立三个 Plus Mapper 并完成最小会话、分页配置。
- [ ] Plus 三个 Mapper 均验证基础 CRUD；课程 Mapper 另验证多条件 Wrapper；选课 Mapper 另验证条件分页、第 2 页和总记录数。

最终规模：原生 MyBatis 共 17 个方法，学生、课程、选课 Mapper 分别有 5、5、7 个方法；Plus 共 3 个 BaseMapper 接口，无自定义业务方法。每个 Mapper 均具备基础 CRUD，扩展功能共同覆盖全部实验要求。


## 五、原生 MyBatis 实施与验证记录

2026-10-19：已实现 Student 5 个、Course 5 个、Enrollment 7 个方法，并移除计划外的原生查询及对应旧测试。Student、Enrollment 使用 XML，Course 使用注解。

按“每个 Mapper 方法对应一个测试方法”整理后执行 `mvn clean test`，结果为 `BUILD SUCCESS`：Student 5 项、Course 5 项、Enrollment 7 项，共 17 项测试，0 failures、0 errors、0 skipped。测试名使用 `test` + Mapper 方法名，每个用例保留简单的数据准备、调用和关键断言；写操作使用临时数据并回滚。

当前测试覆盖各表基础 CRUD、组合模糊查询、部分字段更新、课程各个优先分支及不回退、学生学期查询、批量增删、0 分与未评分，以及空集合删除保护；不再加入独立的复杂异常流程。更新必填项、非空批次和成绩范围等前置校验仍由调用方负责，接口注释说明契约；精简后的测试不再实现或单独测试辅助校验逻辑。

本阶段未修改 Maven 依赖、MyBatis 会话配置、实体、表结构或 Plus 实现。
