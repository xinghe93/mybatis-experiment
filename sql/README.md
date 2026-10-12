# 学生选课数据库脚本

适用于 MySQL 8.0.16 及以上版本，包括当前 Docker 中的 `mysql:8.4`。数据库名为 `mybatis_experiment`，使用 InnoDB、`utf8mb4` 和自增主键。

| 表 | 用途 |
| --- | --- |
| `student` | 学生信息，学号唯一 |
| `course` | 课程信息，课程代码唯一 |
| `enrollment` | 连接学生与课程，保存学期、选课时间和成绩 |

同一学生在同一学期只能选择同一课程一次，不同学期允许再次选择。成绩为 `NULL` 表示尚未评分，零分是有效成绩。学分、学时必须为正数，学分不超过10，成绩范围为0～100。被选课记录引用的学生或课程不能直接删除。

| 文件 | 用途 |
| --- | --- |
| `schema.sql` | 创建数据库、三张表、索引、外键与检查约束 |
| `data.sql` | 首次导入30名学生、8门课程、60条选课记录 |
| `verify.sql` | 检查数量、三表关联、未选课学生、成绩及数据一致性 |

当前容器名为 `mysql8`，已映射宿主机3306端口。在项目根目录将脚本复制到容器，随后进入 MySQL 客户端，按提示输入自己的数据库密码：

```bash
docker cp sql/schema.sql mysql8:/tmp/mybatis-schema.sql
docker cp sql/data.sql mysql8:/tmp/mybatis-data.sql
docker cp sql/verify.sql mysql8:/tmp/mybatis-verify.sql
docker exec -it mysql8 mysql -u root -p --default-character-set=utf8mb4
```

在 `mysql>` 提示符下依次执行，确认上一步成功后再执行下一步：

```sql
SOURCE /tmp/mybatis-schema.sql;
SOURCE /tmp/mybatis-data.sql;
SOURCE /tmp/mybatis-verify.sql;
```

交互式 `SOURCE` 可能在错误后继续运行，数据脚本只在三张新表均为空时执行一次；只读检查应返回30、8、60条记录。

也可以在 IDEA Database 工具中连接 `127.0.0.1:3306`，依次执行这三个文件；后续 Java/JUnit 使用同一地址及数据库名。按实际环境替换容器名、端口和用户名；账号需要建库、建表和写入实验数据的权限。

`schema.sql` 使用 `IF NOT EXISTS`，不清空数据，也不迁移已有表结构。如果之前已经导入员工与部门表，它们会保留，本实验只使用上述三张新表。`data.sql` 只在三张新表均为空时运行一次，重复执行会产生唯一键冲突。

实验数据覆盖4个专业、2023～2026级、两个学期；20条记录已评分（包含零分、满分及3条不及格记录），40条尚未评分。另有10名学生没有选课记录、1门课程没有选课记录，可用于验证空集合映射和LEFT JOIN。学生与教师姓名均为虚构实验数据。

后续换课实验在同一个 `SqlSession` 事务中删除旧选课记录并插入新记录，成功后提交，任一步失败则回滚。业务层需检查旧记录属于该学生且尚未评分、新课程存在、学期一致及无重复选课；学期格式和选课时间也由业务层校验。
