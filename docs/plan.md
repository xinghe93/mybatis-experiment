# MyBatis 课程实验计划

## 一、项目目标

以“学生选课管理”为业务场景，通过 JUnit 演示 MyBatis 和 MyBatis-Plus 的 CRUD、动态 SQL、多条件筛选、分页、多对多关联映射与事务，结合 SQL 日志完成配置和映射问题排查。

采用普通 Maven 项目，使用 Docker 中的 MySQL 8（当前为 `mysql:8.4`，容器名 `mysql8`）、MyBatis、MyBatis-Plus、Log4j 2、JUnit 5 和 Lombok。Java 沿用当前 Maven 配置的25，不开发网页界面。Lombok 用于简化实体和 DTO 的访问方法，并在 Maven 中显式配置注解处理器。

## 二、数据库设计

| 表 | 字段 | 说明 |
| --- | --- | --- |
| `student` | `id`、`student_no`、`name`、`gender`、`major`、`grade`、`enrollment_date` | 学生编号、学号、姓名、性别、专业、入学年级、入学日期 |
| `course` | `id`、`course_no`、`name`、`credit`、`hours`、`teacher` | 课程编号、课程代码、名称、学分、学时、授课教师 |
| `enrollment` | `id`、`student_id`、`course_id`、`semester`、`selected_at`、`score` | 选课记录编号、学生编号、课程编号、学期、选课时间、成绩 |

- 学生与课程通过选课记录建立多对多关系；一名学生可以选择多门课程，一门课程可以有多名学生。
- 学号和课程代码分别唯一；学生编号、课程编号与学期组合唯一，防止同一学期重复选课，不同学期允许重新选修。
- `grade` 表示入学年份；学期统一使用 `2026-2027-1` 形式，末位为1或2。
- 学分和成绩使用 `DECIMAL`，Java 对应 `BigDecimal`；成绩可为空，表示尚未评分，零分是有效成绩。
- 学分范围为大于0且不超过10，学时为正数，成绩范围为0～100。
- 主键采用自增策略；被选课记录引用的学生或课程禁止直接删除。
- 为专业与年级、授课教师、学生与学期、课程与学期设置查询索引。
- 准备30名学生、8门课程、60条选课记录，覆盖4个专业、2023～2026级及两个学期。保留10名未选课学生和1门无人选修课程。

## 三、功能与技术分工

| 实验内容 | 实现方式 |
| --- | --- |
| 学生新增、查询、修改、删除 | MyBatis Mapper 接口 + XML，验证新增后的主键回填 |
| 课程新增、查询、修改、删除 | MyBatis 的 `@Insert`、`@Select`、`@Update`、`@Delete` 注解 |
| 学生多条件筛选 | XML 按学号、姓名、专业、年级、入学日期范围组合查询 |
| 动态 SQL | `<if>`、`<where>` 处理筛选；`<set>` 更新字段；`<foreach>` 处理编号集合；`<choose>` 选择预设排序 |
| SQL 复用 | `<sql>`、`<include>` 复用查询字段和筛选条件 |
| 原生分页 | 手写 `COUNT` 和 `LIMIT`，保证计数与列表使用相同条件及稳定排序 |
| 关联映射 | `resultMap`、`association` 映射选课记录的学生与课程，`collection` 映射学生选课列表及课程选课学生列表 |
| 选课记录查询 | 三表 JOIN 查询学生、课程、教师、学期和成绩，按专业、课程、学期、成绩范围及是否评分筛选并分页 |
| 成绩与选课统计 | 按课程和学期统计人数、已评分人数、平均分，查询不及格学生；LEFT JOIN保留无人选修课程 |
| MyBatis-Plus 通用 CRUD | 三张表分别提供继承 `BaseMapper` 的 Mapper |
| MyBatis-Plus 条件与分页 | 使用 `LambdaQueryWrapper`、`Page` 和 `PaginationInnerInterceptor` |
| 学生换课事务 | 删除旧选课记录，同时新增新课程的选课记录，两步一起提交或回滚 |

原生 Mapper 与 Plus Mapper 分包，分别注册到对应会话工厂。同一个方法不重复使用 XML 和 SQL 注解定义。关联查询为重复的 `id`、`name` 字段设置别名。选课列表分页先分页查询选课记录，再映射关联对象；学生分页带选课集合时先分页学生再加载集合，避免一对多 JOIN 导致人数或页大小错误。

## 四、方法清单

以下为待实现的方法约定。学生 CRUD 使用 XML，课程 CRUD 使用 SQL 注解，选课、关联映射和统计查询使用 XML。三张表另外通过 MyBatis-Plus 演示通用操作。

### 4.1 StudentMapper：学生管理（XML）

| 方法 | 用途与知识点 |
| --- | --- |
| `insert(student)` | 新增学生，回填自增主键 |
| `selectById(id)` | 按学生编号查询 |
| `updateById(student)` | 修改学生信息，使用 `<set>` 动态更新，拒绝没有待更新字段的请求 |
| `deleteById(id)` | 删除学生，被选课记录引用时由外键拒绝 |
| `selectByCondition(query)` | 按学号、姓名、专业、年级、入学日期筛选，使用 `<if>`、`<where>` |
| `selectByIds(ids)` | 使用 `<foreach>` 查询多个学生，空集合返回空列表 |
| `countByCondition(query)` | 统计符合筛选条件的学生数量 |
| `selectPage(query, offset, pageSize)` | 使用 `LIMIT` 分页，使用 `<choose>` 选择预设排序，并以学生编号保证排序稳定 |

`query` 使用 `StudentQuery`，包含筛选条件和预设排序选项。列表、计数及分页查询使用 `<sql>`、`<include>` 复用相同筛选条件；分页参数独立传入，`offset = (pageNo - 1) * pageSize`。

### 4.2 CourseMapper：课程管理（注解）

| 方法 | 用途与注解 |
| --- | --- |
| `insert(course)` | 新增课程，使用 `@Insert`、`@Options` 回填主键 |
| `selectById(id)` | 按课程编号查询，使用 `@Select` |
| `selectAll()` | 查询课程列表，使用 `@Select`，按课程编号排序 |
| `updateById(course)` | 修改课程，使用 `@Update` |
| `deleteById(id)` | 删除课程，使用 `@Delete`，被选课记录引用时由外键拒绝 |

课程 Mapper 重点展示注解 CRUD，不重复实现学生 Mapper 的整套动态查询。

### 4.3 EnrollmentMapper：选课与关联查询（XML）

| 方法 | 用途与知识点 |
| --- | --- |
| `insert(enrollment)` | 新增选课记录，回填主键；唯一约束防止同学期重复选课 |
| `selectById(id)` | 查询选课记录 |
| `updateScore(id, score)` | 录入或修改成绩，合法范围为0～100 |
| `deleteUnscored(id, studentId, semester)` | 删除指定学生、指定学期且尚未评分的选课记录 |
| `selectByIdForUpdate(id)` | 使用 `FOR UPDATE` 锁定旧选课记录，供换课事务使用 |
| `selectDetailById(id)` | 查询选课记录及学生、课程对象，通过 `association` 映射 |
| `selectStudentWithEnrollments(studentId, semester)` | 查询学生及指定学期的选课集合，通过 `collection` 映射，无选课时返回空集合 |
| `selectCourseWithStudents(courseId, semester)` | 查询课程及指定学期的选课学生集合，无人选修时返回空集合 |
| `countByCondition(query)` | 按学生、专业、课程、学期、成绩范围及是否评分计数 |
| `selectPage(query, offset, pageSize)` | 三表关联、多条件筛选与选课记录分页，计数和列表条件保持一致 |
| `selectCourseStatistics(semester)` | 统计各课程在指定学期的选课人数、已评分人数和平均分，保留无人选修课程 |

`query` 使用 `EnrollmentQuery`；“是否评分”采用可选条件，区分未筛选、已评分和未评分，不能把零分当作未评分。三表查询中的重复 `id`、`name` 使用别名。课程统计通过 LEFT JOIN 实现，学期条件放在 JOIN 条件中，避免过滤掉无人选修课程；`AVG` 忽略空成绩，没有已评分记录时平均分为空。

### 4.4 EnrollmentService：业务与事务

| 方法 | 用途 |
| --- | --- |
| `enroll(studentId, courseId, semester)` | 校验学生、课程、学期和选课时间，新增尚未评分的选课记录 |
| `withdraw(studentId, enrollmentId)` | 校验记录归属和成绩状态，只允许退选自己的未评分记录 |
| `changeCourse(studentId, enrollmentId, newCourseId)` | 同一事务中删除旧记录并插入新记录，保持学期一致，失败时回滚 |

`changeCourse` 中的查询、锁定、删除与插入必须使用同一个 `SqlSession`，检查删除影响行数为1后再插入。方法负责显式提交或回滚，并关闭会话；测试通过插入冲突验证旧记录恢复。

### 4.5 MyBatis-Plus：继承通用方法

定义三个接口，不自己重写通用 CRUD 的 SQL：

```java
public interface StudentPlusMapper extends BaseMapper<Student> {}
public interface CoursePlusMapper extends BaseMapper<Course> {}
public interface EnrollmentPlusMapper extends BaseMapper<Enrollment> {}
```

在 JUnit 中调用继承的 `insert`、`selectById`、`updateById`、`deleteById`、`selectList`、`selectCount`、`selectPage`。使用 `LambdaQueryWrapper` 组合条件，通过 `Page` 和 `PaginationInnerInterceptor` 完成分页，并与原生查询比较结果。

### 4.6 会话工厂与实体

| 方法或配置 | 用途 |
| --- | --- |
| `getMyBatisFactory()` | 返回复用的原生 MyBatis `SqlSessionFactory` |
| `getMyBatisPlusFactory()` | 返回复用的 MyBatis-Plus 会话工厂，注册分页插件 |
| Lombok 实体配置 | `Student`、`Course`、`Enrollment` 使用 Lombok 生成访问方法，不手写 getter/setter |

学生和课程编号使用 `Integer`，选课记录编号使用 `Long`；学分和成绩使用 `BigDecimal`，入学日期使用 `LocalDate`，选课时间使用 `LocalDateTime`。Mapper 新增、修改、删除方法返回影响行数，计数方法返回 `long`；单条查询未找到时返回 `null`，列表查询无结果时返回空列表。多参数 Mapper 方法显式标注 `@Param`。

### 4.7 JUnit 测试方法

| 建议测试方法 | 验证内容 |
| --- | --- |
| `testStudentXmlCrud()` | XML CRUD、主键回填、动态更新、学号唯一和外键删除限制 |
| `testCourseAnnotationCrud()` | 注解 CRUD、主键回填、课程代码唯一和外键删除限制 |
| `testDynamicConditions()` | 无条件、单条件、组合条件、预设排序和空编号集合 |
| `testNativePagination()` | 总数、首页、末页、超出页码及排序稳定性 |
| `testAssociationMapping()` | 选课记录的学生和课程对象、重复字段别名 |
| `testCollectionMapping()` | 学生选课集合、课程学生集合，以及空集合 |
| `testEnrollmentConstraints()` | 同学期重复选课失败、跨学期重新选修成功 |
| `testScoreAndStatistics()` | 成绩上下界、零分与空成绩、统计人数和平均分、无人选修课程 |
| `testPlusCrud()` | 三张表的通用 CRUD |
| `testPlusWrapperAndPagination()` | 条件构造器、插件分页，与原生结果对照 |
| `testEnrollAndWithdraw()` | 选课、退课，拒绝退选已评分记录或其他学生的记录 |
| `testChangeCourseCommit()` | 换课成功后旧记录删除、新记录存在 |
| `testChangeCourseRollback()` | 删除旧记录后插入失败，旧记录恢复且无新记录 |

表中的方法按实验主题分组，具体实现时可以拆成多个独立或参数化测试。日志验证检查 SQL、绑定参数及结果数量，并结合断言排查映射问题。

## 五、核心组件与事务

- 原生 MyBatis 使用 `SqlSessionFactoryBuilder` 加载 XML 配置；MyBatis-Plus 使用其对应构建器配置通用 Mapper 与分页插件。
- 两套工厂共享实体和数据库连接参数，工厂复用，每个测试独立获取并关闭 `SqlSession`。
- 显式演示：读取配置 → 创建工厂 → `openSession()` → `getMapper()` → 执行 SQL → 映射结果 → 提交或回滚 → 关闭会话。
- 换课前在同一个会话中锁定旧选课记录，验证其属于该学生且成绩为空、目标课程存在且不同于原课程，保持学期一致。
- 删除旧记录时再次限定学生编号、学期及成绩为空，并检查受影响行数为1，再插入新记录；新记录成绩为空，选课时间为本次操作时间。
- 删除与插入使用同一个 `SqlSession`；任一步失败均回滚。唯一约束处理并发重复选课，发生冲突时旧记录也必须恢复。
- 校验学期格式、选课时间不早于入学日期；本实验不扩展选课容量、课表冲突、学分上限或教师独立实体。
- 依赖版本统一管理，避免独立覆盖 MyBatis 传递依赖；补齐 Plus 分页所需的解析器模块。

## 六、测试与日志

- XML CRUD、注解 CRUD：断言主键、字段值、受影响行数和删除结果；验证学号及课程代码唯一、外键删除限制。
- 动态查询：验证无条件、单条件、组合条件、空集合，以及成绩为空与成绩为零的区别。
- 结果映射：验证学生和课程对象、选课集合、空集合，以及三表关联字段的别名。
- 分页：验证总数、第一页、末页、超出页码以及排序稳定性；页码从1开始，限制每页条数。
- Plus：验证通用 CRUD、条件构造器、分页插件；与原生实现比较相同筛选条件下的记录编号及总数。
- 选课与成绩：验证同学期重复选课失败、不同学期重新选修成功、成绩范围约束及按课程学期统计。
- 事务：验证换课成功，以及删除完成后插入新记录失败时旧记录恢复；拒绝修改已评分记录或其他学生的记录。
- 使用独立实验数据库和固定数据。修改类测试默认回滚，提交类测试清理自身数据，避免测试相互依赖。
- Log4j 2 输出 SQL、参数及类型、结果数量；排查映射问题时开启 TRACE 级别日志。
- 多参数方法显式使用 `@Param`，查询值使用参数绑定，排序选项采用预设白名单。

## 七、建议目录

```text
src/main/java/com/xinghe/
  entity/                 # 学生、课程、选课记录
  dto/                    # 查询条件、分页结果、关联查询结果
  mybatis/mapper/          # 原生 XML 和注解 Mapper
  plus/mapper/             # MyBatis-Plus Mapper
  config/                 # 两套会话工厂及数据库配置加载
  service/                # 学生换课事务
src/main/resources/
  mapper/                 # Mapper XML
  mybatis-config.xml      # 原生 MyBatis 配置
  log4j2.xml              # SQL 日志配置
src/test/java/com/xinghe/ # 按实验内容划分 JUnit 测试
sql/                      # 建表脚本、实验数据和校验脚本
docs/                     # 实验计划、要求对照和排错记录
README.md                 # 环境、连接参数、初始化和运行方式
```

数据库凭据通过环境变量或忽略提交的本地配置提供，不写入仓库。当前 Docker 将3306端口映射到宿主机，Java/JUnit 连接地址为 `jdbc:mysql://127.0.0.1:3306/mybatis_experiment`。

## 八、实施步骤

- [x] 配置 Maven 依赖、JUnit 和 Lombok 注解处理器。
- [x] 编写学生、课程、选课记录的建表、数据与校验脚本。
- [ ] 创建实体，配置数据库连接、两套会话工厂和 Log4j 2。
- [ ] 实现学生 XML CRUD、课程注解 CRUD，并完成对应测试。
- [ ] 实现动态筛选、批量操作、关联映射、成绩统计及原生分页。
- [ ] 实现三张表的 MyBatis-Plus CRUD、条件查询与插件分页。
- [ ] 实现学生换课事务及成功/失败测试。
- [ ] 完善 README、实验要求对照表和实际 AI 辅助排错记录。

方法实现顺序：实体与会话工厂 → 学生 XML CRUD → 课程注解 CRUD → 动态查询与原生分页 → 选课、关联映射与成绩统计 → MyBatis-Plus 条件分页 → 选课、退课与换课事务。每一步完成对应 JUnit 测试后再进入下一步。

## 九、验收与交付

按 README 配置实验数据库并初始化后，执行 `mvn test`，全部测试通过且可以重复运行。每项课程要求均对应具体代码、测试和演示步骤。

交付源码、SQL 脚本、JUnit 测试、日志配置和实验文档。AI 排错记录保留真实问题的现象、日志、分析、修复及回归结果；数据库优化选取实际查询，结合执行计划验证效果。
