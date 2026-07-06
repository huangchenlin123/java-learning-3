# 肉类销售管理系统 — 开发进度

> 最后更新：2026-06-24 | 后端：✅ BUILD SUCCESS（177 源文件）| 前端：✅ BUILD SUCCESS（29 源文件，2237 模块）

## 项目结构

```
meat-sale-management/
├── pom.xml                     # Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Sa-Token 1.37.0
├── sql/init.sql                # 51 张表 DDL + 初始数据（admin / admin123）
├── progress.md                 # 本文件
└── src/main/java/com/meat/
    ├── MeatApplication.java    # 启动类（端口 8080）
    ├── common/                 # 公共模块
    │   ├── R.java              # 统一返回 {code, msg, data}
    │   ├── BaseEntity.java     # 实体基类（id, createTime, updateTime, deleted）
    │   ├── annotation/         # @OperLog, @DataScope
    │   ├── aspect/OpLogAspect  # AOP 操作日志
    │   ├── config/             # SaToken, MybatisPlus, Knife4j, Redis, WebMVC
    │   ├── exception/          # BusinessException, GlobalExceptionHandler
    │   ├── handler/            # MyMetaObjectHandler（自动填充）
    │   └── interceptor/        # DataScopeInterceptor
    ├── system/                 # ✅ 系统管理（15 文件）
    │   ├── entity/             # SysUser, SysRole, SysMenu, SysDict, SysDictItem
    │   ├── mapper/             # + 注解 SQL（权限查询、角色菜单关联）
    │   ├── service/            # 登录认证（BCrypt + Sa-Token）、RBAC 权限、菜单树
    │   └── controller/         # Auth, User, Role, Menu, Dict（完整 REST API）
    ├── purchase/               # ✅ 采购管理（23 文件）
    │   ├── entity/             # Supplier, GoodsCategory, GoodsSku
    │   │                       # PurchaseOrder/Item, PurchaseReceipt/Item, PurchaseReturn/Item
    │   ├── mapper/             # 全部 CRUD Mapper
    │   ├── service/            # 采购订单审批流、验收→自动生成入库单→批次创建
    │   └── controller/         # Category, SKU, Supplier, PurchaseOrder, PurchaseReceipt
    ├── warehouse/              # ✅ 仓库管理（55 文件）
    │   ├── entity/             # Warehouse, Shelf, Batch, BatchExpiryWarning, Stock, StockIn/Item
    │   │                       # StockOut/Item, TransferOrder/Item
    │   │                       # ProcessOrder, ProcessBom/Item, ProcessItemOut
    │   │                       # InventoryCheck/Item, ScrapOrder/Item
    │   ├── mapper/             # 全部 CRUD Mapper
    │   ├── scheduler/          # ExpiryWarningScheduler（每天 8:00 效期扫描）
    │   ├── service/            # StockInServiceImpl, ProcessOrderServiceImpl（加工+成本分摊）
    │   │                       # ExpiryWarningServiceImpl（效期预警）
    │   └── controller/         # Warehouse, Shelf, StockIn, Transfer, Process
    │                           # ExpiryWarning, InventoryCheck, Scrap
    ├── sale/                   # ✅ 销售管理（28 文件）
    │   ├── entity/             # Customer, Member, MemberLevel, MemberPoints
    │   │                       # Promotion, PromotionRule
    │   │                       # SaleOrder/Item, SaleReturn/Item
    │   ├── mapper/             # 全部 CRUD Mapper
    │   ├── service/            # SaleOrderServiceImpl（POS 收银 + FIFO 批次出库 + 库存扣减）
    │   └── controller/         # SaleOrder（/pos 收银接口）, Customer, Member, Promotion
    ├── promotion/              # ✅ 促销取价引擎（11 文件）
    │   ├── strategy/           # PromotionStrategy 接口 + 4 种策略实现
    │   │                       #   FullReduction(满减) / Discount(折扣)
    │   │                       #   FlashSale(限时特价) / BuyGift(买赠)
    │   ├── engine/             # PromotionEngine(链式引擎) + PromotionResult
    │   └── controller/         # PromotionController（活动+规则 CRUD + 试算）
    ├── finance/                # ✅ 财务管理（10 文件）
    │   ├── entity/             # PriceRecord, Payment, PaymentItem
    │   ├── mapper/             # 全部 CRUD Mapper
    │   ├── service/            # ReportService + ReportServiceImpl（7 个真实报表）
    │   └── controller/         # FinanceController（经营概览/销售/品类/对账/库存/毛利）
    └── trace/                  # ✅ 溯源（3 文件）
        ├── entity/             # ColdChainLog, TraceRecord
        └── mapper/             # CRUD Mapper
```

## 核心业务链路（已实现）

| 链路 | Controller | Service 方法 | 状态 |
|------|-----------|-------------|:--:|
| 登录认证 | POST /api/auth/login | SysUserServiceImpl.login() | ✅ |
| 采购下单→审批 | POST/PUT /api/purchase/order | PurchaseOrderServiceImpl.save()/approve() | ✅ |
| 验收→生成入库单→批次 | POST/PUT /api/purchase/receipt | PurchaseReceiptServiceImpl.confirmReceipt() | ✅ |
| 入库确认→库存更新 | PUT /api/warehouse/stockin/{id}/confirm | StockInServiceImpl.confirm() | ✅ |
| POS 收银→FIFO 出库 | POST /api/sale/order/pos | SaleOrderServiceImpl.posSale() | ✅ |
| 促销取价（策略模式） | POST /api/promotion/preview | PromotionEngine.calculate() | ✅ |
| 分割加工→成本分摊 | PUT /api/warehouse/process/{id}/complete | ProcessOrderServiceImpl.completeProcess() | ✅ |
| 效期预警定时扫描 | @Scheduled(cron=0 0 8 * * ?) | ExpiryWarningScheduler.scanExpiry() | ✅ |
| 经营概览 + 7 报表 | GET /api/finance/dashboard 等 | ReportServiceImpl（真实 DB 查询） | ✅ |
| 批发订单审批 | PUT /api/sale/order/{id}/approve | SaleOrderServiceImpl.approveOrder() | ✅ |

## 环境依赖

| 组件 | 版本 | 端口 | 状态 |
|------|------|------|:--:|
| JDK | 17.0.18 | — | ✅ |
| MySQL | 8.0.46 | 3306 | ✅ 运行中 |
| Redis | — | 6379 | ✅ 运行中 |
| Maven | — | — | ✅ |

## 启动步骤

```bash
# 1. 初始化数据库
mysql -u root -p < sql/init.sql

# 2. 修改 application.yml 中的数据库密码

# 3. 启动
cd meat-sale-management
mvn spring-boot:run

# 4. 访问 API 文档
# http://localhost:8080/doc.html
# 默认账号: admin / admin123
```

## 下一步计划

1. ~~促销取价引擎（策略模式实现）~~ ✅ 已完成
2. ~~分割加工成本分摊算法完善~~ ✅ 已完成
3. ~~效期预警定时任务~~ ✅ 已完成
4. ~~报表数据接口实现~~ ✅ 已完成
5. ~~Vue 3 + Element Plus 前端管理后台~~ ✅ 已完成
