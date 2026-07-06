-- ============================================
-- 肉类销售管理系统 — 数据库初始化脚本
-- DB: MySQL 8.0
-- ============================================

CREATE DATABASE IF NOT EXISTS meat_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE meat_db;

-- ============================================
-- 一、系统管理模块
-- ============================================

-- 1. 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    warehouse_id BIGINT COMMENT '所属仓库ID(数据权限)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='系统用户表';

-- 2. 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '描述',
    data_scope TINYINT DEFAULT 1 COMMENT '数据范围 1:全部 2:本仓库 3:仅本人',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 3. 菜单权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT DEFAULT 1 COMMENT '类型 1:目录 2:菜单 3:按钮',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(200) COMMENT '组件路径',
    perms VARCHAR(200) COMMENT '权限标识',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB COMMENT='菜单权限表';

-- 4. 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 5. 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- 6. 数据字典表
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(50) NOT NULL COMMENT '字典名称',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB COMMENT='数据字典表';

-- 7. 数据字典项表
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_id BIGINT NOT NULL COMMENT '字典ID',
    item_label VARCHAR(50) NOT NULL COMMENT '字典项标签',
    item_value VARCHAR(50) NOT NULL COMMENT '字典项值',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB COMMENT='数据字典项表';

-- 8. 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人',
    module VARCHAR(50) COMMENT '操作模块',
    type VARCHAR(50) COMMENT '操作类型',
    description VARCHAR(500) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration BIGINT COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='操作日志表';

-- ============================================
-- 二、基础数据模块
-- ============================================

-- 9. 商品分类表
DROP TABLE IF EXISTS goods_category;
CREATE TABLE goods_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) COMMENT '分类编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB COMMENT='商品分类表';

-- 10. 商品SKU表
DROP TABLE IF EXISTS goods_sku;
CREATE TABLE goods_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '分类ID',
    sku_code VARCHAR(50) NOT NULL COMMENT '商品编码',
    sku_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    unit VARCHAR(10) DEFAULT 'kg' COMMENT '计量单位',
    barcode VARCHAR(50) COMMENT '条形码',
    default_price DECIMAL(10,2) DEFAULT 0 COMMENT '默认售价',
    safety_stock DECIMAL(10,2) DEFAULT 0 COMMENT '安全库存(kg)',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_sku_code (sku_code)
) ENGINE=InnoDB COMMENT='商品SKU表';

-- 11. 仓库表
DROP TABLE IF EXISTS warehouse;
CREATE TABLE warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(50) NOT NULL COMMENT '仓库名称',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '仓库编码',
    warehouse_type VARCHAR(20) DEFAULT 'COLD' COMMENT '类型:FROZEN冷冻/COLD冷藏/NORMAL常温',
    address VARCHAR(200) COMMENT '地址',
    manager VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_warehouse_code (warehouse_code)
) ENGINE=InnoDB COMMENT='仓库表';

-- 12. 货架表
DROP TABLE IF EXISTS shelf;
CREATE TABLE shelf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    shelf_code VARCHAR(50) NOT NULL COMMENT '货架编码',
    shelf_name VARCHAR(50) COMMENT '货架名称',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_shelf (warehouse_id, shelf_code)
) ENGINE=InnoDB COMMENT='货架表';

-- 13. 供应商表
DROP TABLE IF EXISTS supplier;
CREATE TABLE supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    supplier_code VARCHAR(50) COMMENT '供应商编码',
    contact_person VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(200) COMMENT '地址',
    license_no VARCHAR(100) COMMENT '营业执照号',
    quarantine_cert VARCHAR(200) COMMENT '检疫资质证照(文件路径)',
    rating TINYINT DEFAULT 3 COMMENT '评级 1-5',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB COMMENT='供应商表';

-- 14. 供应商商品关联表
DROP TABLE IF EXISTS supplier_goods;
CREATE TABLE supplier_goods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    price DECIMAL(10,2) COMMENT '供应价格',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_supplier_sku (supplier_id, sku_id)
) ENGINE=InnoDB COMMENT='供应商商品表';

-- ============================================
-- 三、采购管理模块
-- ============================================

-- 15. 采购订单表
DROP TABLE IF EXISTS purchase_order;
CREATE TABLE purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL COMMENT '采购单号',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '采购总金额',
    expect_arrive_time DATE COMMENT '预计到货日期',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审批/APPROVED已审批/SHIPPED已发货/RECEIVED已验收/COMPLETED已完成/CANCELLED已取消',
    approve_by BIGINT COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='采购订单表';

-- 16. 采购订单明细表
DROP TABLE IF EXISTS purchase_order_item;
CREATE TABLE purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '采购单ID',
    sku_id BIGINT NOT NULL COMMENT '商品ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '采购数量(kg)',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='采购订单明细表';

-- 17. 采购验收表
DROP TABLE IF EXISTS purchase_receipt;
CREATE TABLE purchase_receipt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '采购单ID',
    receipt_no VARCHAR(50) NOT NULL COMMENT '验收单号',
    actual_weight DECIMAL(10,2) COMMENT '实际称重(kg)',
    quality_result TINYINT DEFAULT 1 COMMENT '质检结果 1:通过 0:不通过',
    quarantine_no VARCHAR(100) COMMENT '检疫证号',
    origin VARCHAR(200) COMMENT '产地',
    slaughter_date DATE COMMENT '屠宰日期',
    diff_ratio DECIMAL(5,2) COMMENT '差异比例(%)',
    diff_handled TINYINT DEFAULT 0 COMMENT '差异是否已处理',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待确认/CONFIRMED已确认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_receipt_no (receipt_no)
) ENGINE=InnoDB COMMENT='采购验收表';

-- 18. 采购验收明细表
DROP TABLE IF EXISTS purchase_receipt_item;
CREATE TABLE purchase_receipt_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL COMMENT '订单明细ID',
    sku_id BIGINT NOT NULL,
    order_quantity DECIMAL(10,2) COMMENT '订单数量',
    actual_quantity DECIMAL(10,2) COMMENT '实收数量',
    price DECIMAL(10,2) COMMENT '单价',
    quality_status TINYINT DEFAULT 1 COMMENT '质检状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='验收明细表';

-- 19. 采购退货单
DROP TABLE IF EXISTS purchase_return;
CREATE TABLE purchase_return (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_no VARCHAR(50) NOT NULL COMMENT '退货单号',
    receipt_id BIGINT COMMENT '关联验收单ID',
    supplier_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '退货总金额',
    return_reason VARCHAR(500) COMMENT '退货原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待出库/COMPLETED已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_return_no (return_no)
) ENGINE=InnoDB COMMENT='采购退货单';

-- 20. 采购退货明细表
DROP TABLE IF EXISTS purchase_return_item;
CREATE TABLE purchase_return_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL COMMENT '退货数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='采购退货明细表';

-- ============================================
-- 四、库存管理核心表
-- ============================================

-- 21. 批次表
DROP TABLE IF EXISTS batch;
CREATE TABLE batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号(PO/PROC/TR+yyyyMMdd+4位流水)',
    sku_id BIGINT NOT NULL COMMENT '商品ID',
    product_date DATE COMMENT '生产日期/包装日期',
    shelf_life INT COMMENT '保质期(天)',
    expire_date DATE COMMENT '到期日',
    source_type VARCHAR(20) NOT NULL COMMENT '来源类型:PURCHASE/PROCESS/TRANSFER',
    source_id BIGINT COMMENT '来源单据ID',
    quarantine_no VARCHAR(100) COMMENT '检疫证号',
    origin VARCHAR(200) COMMENT '产地',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_batch_no (batch_no),
    INDEX idx_sku (sku_id),
    INDEX idx_expire (expire_date)
) ENGINE=InnoDB COMMENT='批次表';

-- 22. 库存表
DROP TABLE IF EXISTS stock;
CREATE TABLE stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL COMMENT '商品ID',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    shelf_id BIGINT COMMENT '货架ID',
    quantity DECIMAL(10,2) DEFAULT 0 COMMENT '库存数量(kg)',
    locked_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '锁定数量(kg)',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stock (sku_id, batch_id, warehouse_id),
    INDEX idx_warehouse (warehouse_id)
) ENGINE=InnoDB COMMENT='库存表';

-- 23. 入库单表
DROP TABLE IF EXISTS stock_in;
CREATE TABLE stock_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    in_no VARCHAR(50) NOT NULL COMMENT '入库单号',
    in_type VARCHAR(20) NOT NULL COMMENT '入库类型:PURCHASE采购/PROCESS加工/TRANSFER调拨/RETURN退货返库',
    source_id BIGINT COMMENT '来源单据ID',
    warehouse_id BIGINT NOT NULL,
    total_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '入库总量(kg)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待确认/CONFIRMED已确认',
    confirm_by BIGINT COMMENT '确认人',
    confirm_time DATETIME COMMENT '确认时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_in_no (in_no),
    INDEX idx_warehouse (warehouse_id)
) ENGINE=InnoDB COMMENT='入库单表';

-- 24. 入库明细表
DROP TABLE IF EXISTS stock_in_item;
CREATE TABLE stock_in_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    in_id BIGINT NOT NULL COMMENT '入库单ID',
    sku_id BIGINT NOT NULL,
    batch_id BIGINT COMMENT '批次ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '入库数量(kg)',
    cost_price DECIMAL(10,2) COMMENT '成本价',
    shelf_id BIGINT COMMENT '货架ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='入库明细表';

-- 25. 出库单表
DROP TABLE IF EXISTS stock_out;
CREATE TABLE stock_out (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    out_no VARCHAR(50) NOT NULL COMMENT '出库单号',
    out_type VARCHAR(20) NOT NULL COMMENT '出库类型:SALE销售/PROCESS加工/TRANSFER调拨/SCRAP报损/PURCHASE_RETURN采购退货',
    source_id BIGINT COMMENT '来源单据ID',
    warehouse_id BIGINT NOT NULL,
    total_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '出库总量(kg)',
    status VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_out_no (out_no),
    INDEX idx_warehouse (warehouse_id)
) ENGINE=InnoDB COMMENT='出库单表';

-- 26. 出库明细表
DROP TABLE IF EXISTS stock_out_item;
CREATE TABLE stock_out_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    out_id BIGINT NOT NULL COMMENT '出库单ID',
    sku_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL COMMENT '批次ID(FIFO优先)',
    quantity DECIMAL(10,2) NOT NULL COMMENT '出库数量(kg)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='出库明细表';

-- 27. 调拨单表
DROP TABLE IF EXISTS transfer_order;
CREATE TABLE transfer_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_no VARCHAR(50) NOT NULL COMMENT '调拨单号',
    from_warehouse_id BIGINT NOT NULL COMMENT '源仓库ID',
    to_warehouse_id BIGINT NOT NULL COMMENT '目标仓库ID',
    total_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '调拨总量(kg)',
    reason VARCHAR(500) COMMENT '调拨原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审批/APPROVED已审批/COMPLETED已完成/REJECTED已驳回',
    approve_by BIGINT COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    create_by BIGINT COMMENT '申请人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_transfer_no (transfer_no)
) ENGINE=InnoDB COMMENT='调拨单表';

-- 28. 调拨明细表
DROP TABLE IF EXISTS transfer_order_item;
CREATE TABLE transfer_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_id BIGINT NOT NULL COMMENT '调拨单ID',
    sku_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL COMMENT '调拨数量(kg)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='调拨明细表';

-- ============================================
-- 五、分割加工模块
-- ============================================

-- 29. 加工单表
DROP TABLE IF EXISTS process_order;
CREATE TABLE process_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_no VARCHAR(50) NOT NULL COMMENT '加工单号',
    bom_id BIGINT COMMENT 'BOM模板ID',
    warehouse_id BIGINT NOT NULL,
    raw_sku_id BIGINT NOT NULL COMMENT '原料商品ID',
    raw_batch_id BIGINT NOT NULL COMMENT '原料批次ID',
    raw_quantity DECIMAL(10,2) NOT NULL COMMENT '加工数量(kg)',
    raw_cost DECIMAL(12,2) COMMENT '原料成本',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待加工/PROCESSING加工中/COMPLETED已完成',
    create_by BIGINT COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_process_no (process_no)
) ENGINE=InnoDB COMMENT='加工单表';

-- 30. 加工BOM模板表
DROP TABLE IF EXISTS process_bom;
CREATE TABLE process_bom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bom_name VARCHAR(100) NOT NULL COMMENT 'BOM名称(如:半片猪标准分割)',
    raw_sku_id BIGINT NOT NULL COMMENT '原料商品ID',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='加工BOM模板表';

-- 31. BOM明细表
DROP TABLE IF EXISTS process_bom_item;
CREATE TABLE process_bom_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bom_id BIGINT NOT NULL COMMENT 'BOM ID',
    output_sku_id BIGINT NOT NULL COMMENT '产出商品ID',
    output_ratio DECIMAL(5,2) NOT NULL COMMENT '产出比例(%)',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='BOM明细表';

-- 32. 加工产出明细表
DROP TABLE IF EXISTS process_item_out;
CREATE TABLE process_item_out (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_id BIGINT NOT NULL COMMENT '加工单ID',
    sku_id BIGINT NOT NULL COMMENT '产出商品ID',
    batch_no VARCHAR(50) COMMENT '新批次号',
    expected_quantity DECIMAL(10,2) COMMENT '预计产出(kg)',
    actual_quantity DECIMAL(10,2) COMMENT '实际产出(kg)',
    cost_price DECIMAL(10,2) COMMENT '分摊成本价',
    waste_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '损耗(kg)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='加工产出明细表';

-- 33. 批次效期预警表
DROP TABLE IF EXISTS batch_expiry_warning;
CREATE TABLE batch_expiry_warning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    sku_id BIGINT NOT NULL COMMENT '商品ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    quantity DECIMAL(10,2) COMMENT '库存数量(kg)',
    expire_date DATE NOT NULL COMMENT '到期日期',
    days_remaining INT COMMENT '距到期天数',
    warn_level VARCHAR(20) COMMENT 'CRITICAL/WARNING/NOTICE',
    is_handled TINYINT DEFAULT 0 COMMENT '是否已处理',
    handled_by BIGINT COMMENT '处理人',
    handled_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='批次效期预警记录';

-- ============================================
-- 六、盘点与报损模块
-- ============================================

-- 33. 盘点单表
DROP TABLE IF EXISTS inventory_check;
CREATE TABLE inventory_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_no VARCHAR(50) NOT NULL COMMENT '盘点单号',
    warehouse_id BIGINT NOT NULL,
    freeze_stock TINYINT DEFAULT 0 COMMENT '是否冻结库存',
    check_range VARCHAR(500) COMMENT '盘点范围',
    status VARCHAR(20) DEFAULT 'CHECKING' COMMENT 'CHECKING盘点中/SUBMITTED已提交/APPROVED已审核/ADJUSTED已调整',
    create_by BIGINT COMMENT '盘点人',
    submit_time DATETIME COMMENT '提交时间',
    approve_by BIGINT COMMENT '审核人',
    approve_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_check_no (check_no)
) ENGINE=InnoDB COMMENT='盘点单表';

-- 34. 盘点明细表
DROP TABLE IF EXISTS inventory_check_item;
CREATE TABLE inventory_check_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_id BIGINT NOT NULL COMMENT '盘点单ID',
    sku_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    book_quantity DECIMAL(10,2) COMMENT '账面数量(kg)',
    actual_quantity DECIMAL(10,2) COMMENT '实盘数量(kg)',
    diff_quantity DECIMAL(10,2) COMMENT '差异数量(kg)',
    diff_reason VARCHAR(500) COMMENT '差异原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='盘点明细表';

-- 35. 报损单表
DROP TABLE IF EXISTS scrap_order;
CREATE TABLE scrap_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scrap_no VARCHAR(50) NOT NULL COMMENT '报损单号',
    warehouse_id BIGINT NOT NULL,
    total_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '报损总量(kg)',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '报损金额(按成本价)',
    scrap_reason VARCHAR(500) COMMENT '报损原因',
    photo_url VARCHAR(500) COMMENT '照片路径',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审批/APPROVED已审批/COMPLETED已出库',
    approve_by BIGINT COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    create_by BIGINT COMMENT '申请人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_scrap_no (scrap_no)
) ENGINE=InnoDB COMMENT='报损单表';

-- 36. 报损明细表
DROP TABLE IF EXISTS scrap_order_item;
CREATE TABLE scrap_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scrap_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL COMMENT '报损数量(kg)',
    cost_price DECIMAL(10,2) COMMENT '成本价',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='报损明细表';

-- ============================================
-- 七、销售管理模块
-- ============================================

-- 37. 客户表
DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    customer_type VARCHAR(20) DEFAULT 'RETAIL' COMMENT 'RETAIL零售/WHOLESALE批发/ENTERPRISE企业',
    contact_person VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(200) COMMENT '地址',
    tax_no VARCHAR(50) COMMENT '税号(批发客户)',
    credit_limit DECIMAL(12,2) DEFAULT 0 COMMENT '信用额度',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB COMMENT='客户表';

-- 38. 会员表
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_no VARCHAR(50) NOT NULL COMMENT '会员卡号',
    member_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    id_card VARCHAR(20) COMMENT '身份证号',
    level_id BIGINT COMMENT '会员等级ID',
    total_points INT DEFAULT 0 COMMENT '累计积分',
    available_points INT DEFAULT 0 COMMENT '可用积分',
    total_consumption DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费金额',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_card_no (card_no),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB COMMENT='会员表';

-- 39. 会员等级表
DROP TABLE IF EXISTS member_level;
CREATE TABLE member_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    level_code VARCHAR(20) NOT NULL COMMENT '等级编码',
    min_consumption DECIMAL(12,2) DEFAULT 0 COMMENT '最低消费金额(升级阈值)',
    discount_rate DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率',
    points_rate DECIMAL(3,2) DEFAULT 1.00 COMMENT '积分倍率',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='会员等级表';

-- 40. 积分流水表
DROP TABLE IF EXISTS member_points;
CREATE TABLE member_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    points_change INT NOT NULL COMMENT '积分变动(+收入/-支出)',
    change_type VARCHAR(20) COMMENT '变动类型:EARN获得/USE使用/EXPIRE过期/ADJUST调整',
    order_id BIGINT COMMENT '关联订单ID',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member (member_id)
) ENGINE=InnoDB COMMENT='积分流水表';

-- 41. 促销活动表
DROP TABLE IF EXISTS promotion;
CREATE TABLE promotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promo_name VARCHAR(100) NOT NULL COMMENT '活动名称',
    promo_type VARCHAR(20) NOT NULL COMMENT 'FLASH_SALE限时特价/FULL_REDUCTION满减/DISCOUNT折扣/BUY_GIFT买赠',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    priority INT DEFAULT 0 COMMENT '优先级(越大越高)',
    status TINYINT DEFAULT 1 COMMENT '状态 0:停用 1:启用',
    description VARCHAR(500) COMMENT '活动说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='促销活动表';

-- 42. 促销规则表
DROP TABLE IF EXISTS promotion_rule;
CREATE TABLE promotion_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promo_id BIGINT NOT NULL COMMENT '促销ID',
    rule_type VARCHAR(20) NOT NULL COMMENT '规则类型:CONDITION条件/ACTION动作',
    sku_id BIGINT COMMENT '适用商品ID(空=全场)',
    category_id BIGINT COMMENT '适用分类ID(空=不限)',
    threshold DECIMAL(12,2) COMMENT '阈值(满减金额/满赠件数)',
    discount DECIMAL(12,2) COMMENT '优惠(减额/折扣率/特价/赠品数量)',
    gift_sku_id BIGINT COMMENT '赠品商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='促销规则表';

-- 43. 销售订单表
DROP TABLE IF EXISTS sale_order;
CREATE TABLE sale_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL COMMENT '销售单号',
    order_type VARCHAR(20) DEFAULT 'RETAIL' COMMENT 'RETAIL零售/WHOLESALE批发',
    customer_id BIGINT COMMENT '客户ID',
    member_id BIGINT COMMENT '会员ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '原价总额',
    discount_amount DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
    final_amount DECIMAL(12,2) DEFAULT 0 COMMENT '实付金额',
    payment_method VARCHAR(50) COMMENT '支付方式',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审核/APPROVED已审核/PICKING备货中/SHIPPED已出库/COMPLETED已完成/CANCELLED已取消',
    approve_by BIGINT COMMENT '审核人',
    create_by BIGINT COMMENT '销售员',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sale_no (order_no),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='销售订单表';

-- 44. 销售订单明细表
DROP TABLE IF EXISTS sale_order_item;
CREATE TABLE sale_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '销售单ID',
    sku_id BIGINT NOT NULL COMMENT '商品ID',
    batch_id BIGINT COMMENT '出库批次ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '数量(kg)',
    original_price DECIMAL(10,2) COMMENT '原价',
    final_price DECIMAL(10,2) COMMENT '成交价',
    amount DECIMAL(12,2) COMMENT '金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='销售明细表';

-- 45. 销售退货单
DROP TABLE IF EXISTS sale_return;
CREATE TABLE sale_return (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_no VARCHAR(50) NOT NULL COMMENT '退货单号',
    order_id BIGINT NOT NULL COMMENT '原销售单ID',
    customer_id BIGINT COMMENT '客户ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '退款总额',
    return_reason VARCHAR(500) COMMENT '退货原因',
    quality_result TINYINT COMMENT '质检结果 1:可售 0:报损',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待质检/COMPLETED已完成',
    create_by BIGINT COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_return_sale_no (return_no)
) ENGINE=InnoDB COMMENT='销售退货单';

-- 46. 销售退货明细表
DROP TABLE IF EXISTS sale_return_item;
CREATE TABLE sale_return_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL COMMENT '原明细ID',
    sku_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL COMMENT '退货数量(kg)',
    price DECIMAL(10,2) COMMENT '单价(原成交价)',
    amount DECIMAL(12,2) COMMENT '退款金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='销售退货明细表';

-- ============================================
-- 八、财务管理模块
-- ============================================

-- 47. 价格记录表
DROP TABLE IF EXISTS price_record;
CREATE TABLE price_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL COMMENT '商品ID',
    price_type VARCHAR(20) DEFAULT 'STANDARD' COMMENT 'STANDARD标准价/MEMBER会员价/PROMOTION促销价',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    member_level_id BIGINT COMMENT '会员等级ID(会员价时使用)',
    effective_time DATETIME COMMENT '生效时间',
    create_by BIGINT COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sku (sku_id)
) ENGINE=InnoDB COMMENT='价格记录表';

-- 48. 收款记录表
DROP TABLE IF EXISTS payment;
CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(50) NOT NULL COMMENT '收款单号',
    order_id BIGINT NOT NULL COMMENT '销售单ID',
    order_type VARCHAR(20) COMMENT 'SALE销售/RETURN退款',
    payment_method VARCHAR(20) COMMENT 'CASH现金/WECHAT微信/ALIPAY支付宝/BANK银行卡/COMBO组合',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    create_by BIGINT COMMENT '收银员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_no (payment_no),
    INDEX idx_order (order_id)
) ENGINE=InnoDB COMMENT='收款记录表';

-- 49. 收款明细表(组合支付时使用)
DROP TABLE IF EXISTS payment_item;
CREATE TABLE payment_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    reference_no VARCHAR(100) COMMENT '第三方流水号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='收款明细表';

-- ============================================
-- 九、冷链与溯源模块
-- ============================================

-- 50. 冷链温度记录表
DROP TABLE IF EXISTS cold_chain_log;
CREATE TABLE cold_chain_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    temperature DECIMAL(5,1) COMMENT '温度(℃)',
    humidity DECIMAL(5,1) COMMENT '湿度(%)',
    is_abnormal TINYINT DEFAULT 0 COMMENT '是否异常',
    abnormal_desc VARCHAR(200) COMMENT '异常描述',
    record_by BIGINT COMMENT '记录人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_warehouse_time (warehouse_id, create_time)
) ENGINE=InnoDB COMMENT='冷链温度记录表';

-- 51. 溯源记录表
DROP TABLE IF EXISTS trace_record;
CREATE TABLE trace_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    node_type VARCHAR(20) NOT NULL COMMENT '节点类型:PURCHASE采购/STOCK_IN入库/PROCESS加工/SALE销售/SCRAP报损',
    node_id BIGINT COMMENT '节点单据ID',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch (batch_no)
) ENGINE=InnoDB COMMENT='溯源记录表';

-- ============================================
-- 十、初始化数据
-- ============================================

-- 默认管理员 (密码: admin123, 加密后)
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$AIQ6I0rFkUpSLafGlwxQGuJMXabIeEIxO6ntwVn1nKjLvaI/nbpAi', '系统管理员', 1);

-- 默认角色
INSERT INTO sys_role (role_name, role_code, data_scope, description) VALUES
('系统管理员', 'ADMIN', 1, '拥有全部权限'),
('采购员', 'BUYER', 1, '采购管理'),
('仓库管理员', 'WAREHOUSE', 1, '仓库管理'),
('销售员', 'SALES', 1, '销售管理'),
('财务经理', 'MANAGER', 1, '财务管理与审批');

-- 管理员绑定角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 默认菜单(一级)
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, icon, sort) VALUES
(0, '系统管理', 1, '/system', 'Setting', 1),
(0, '基础数据', 1, '/base', 'Document', 2),
(0, '采购管理', 1, '/purchase', 'ShoppingCart', 3),
(0, '仓库管理', 1, '/warehouse', 'Box', 4),
(0, '销售管理', 1, '/sale', 'Sell', 5),
(0, '财务管理', 1, '/finance', 'Money', 6),
(0, '溯源查询', 1, '/trace', 'Search', 7);

-- 默认仓库
INSERT INTO warehouse (warehouse_name, warehouse_code, warehouse_type) VALUES
('主冷库', 'CK001', 'COLD'),
('冷冻库', 'CK002', 'FROZEN'),
('常温库', 'CK003', 'NORMAL');

-- 默认会员等级
INSERT INTO member_level (level_name, level_code, min_consumption, discount_rate, points_rate) VALUES
('普通会员', 'NORMAL', 0, 1.00, 1.00),
('银卡会员', 'SILVER', 5000, 0.95, 1.20),
('金卡会员', 'GOLD', 20000, 0.90, 1.50);
