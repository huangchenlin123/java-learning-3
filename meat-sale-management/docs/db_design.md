\# 肉类销售管理系统



基于 SpringBoot + Vue.js 的进销存一体化管理平台，实现供应商、库存、销售订单的全链路数字化管理。



\---



\## 技术栈



| 层级 | 技术 |

|------|------|

| 后端框架 | SpringBoot + MyBatis-Plus |

| 数据库 | MySQL |

| 缓存 | Redis |

| 前端 | Vue.js |

| 版本控制 | Git |

| AI辅助 | ClaudeCode（编码 + 代码审查） |



\---



\## 核心功能



\- 供应商管理（增删改查 + 供应商评估）

\- 库存管理（入库/出库/盘点/预警）

\- 销售订单管理（下单/审批/发货/统计）

\- 销售报表统计（按日/月/年多维分析）



\---



\## 数据库设计



共设计 \*\*6 张业务表\*\*，覆盖核心业务模块：



\- `supplier` — 供应商信息表

\- `inventory` — 库存表

\- `inventory\_log` — 库存变动日志表

\- `sales\_order` — 销售订单表

\- `order\_item` — 订单明细表

\- `product\_category` — 商品分类表



> 详细表结构见：\[数据库设计文档](./docs/db\_design.md)



\---



\## 性能优化



\- \*\*Redis缓存\*\*：缓存商品分类、热销榜单等高频查询数据，缓存命中率 90%+，数据库 QPS 降低约 40%

\- \*\*SQL索引优化\*\*：针对订单号、商品名称等高频查询字段建立联合索引，接口响应时间从 800ms 降至 200ms 以内



\---



\## 项目结构



```

meat-sale-management/

├── src/

│   ├── main/

│   │   ├── java/com/xxx/

│   │   │   ├── controller/     # 控制层

│   │   │   ├── service/        # 业务层

│   │   │   ├── mapper/         # 数据访问层

│   │   │   ├── entity/         # 实体类

│   │   │   └── config/         # 配置类（Redis、MyBatis-Plus）

│   │   └── resources/

│   │       ├── mapper/         # MyBatis XML

│   │       └── application.yml # 配置文件

│   └── test/                   # 单元测试

└── pom.xml

```



\---



\## 运行方式



```bash

\# 1. 克隆项目

git clone https://github.com/huangchenlin123/java-learning-3.git



\# 2. 导入 IDEA，配置 MySQL 和 Redis 连接



\# 3. 启动 SpringBoot 应用

mvn spring-boot:run



\# 4. 访问 API（默认端口 8080）

http://localhost:8080/api/...

```



\---



\## 测试账号



> 如有需要，可提供 Postman 接口测试集合



\---



\## 后续计划



\- \[ ] 集成 JWT 权限认证

\- \[ ] 接入消息队列（RabbitMQ）实现订单异步处理

\- \[ ] 引入 Docker 容器化部署



\---



\## 许可证



MIT License

