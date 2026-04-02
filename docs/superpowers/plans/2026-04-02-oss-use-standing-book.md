# 使用台账管理功能实现方案

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增使用台账管理功能，包含Main表（台账主记录）和Detail表（台账明细），支持列表查询、详情弹窗（按sw_category显示不同字段）、以及Import导入功能。

**Architecture:**
- 后端：Spring Boot + MyBatis-Plus，Main/Detail双表结构，parent_id关联
- 前端：Vue 3 + Element Plus，复用现有oss目录结构
- Import导入时同一数据同时写入Main和Detail，冗余字段保持一致

**Tech Stack:** Spring Boot 3.2.0, MyBatis-Plus 3.5.5, Vue 3, Element Plus 2.5.5

---

## 文件结构

```
bank-it-arch-backend/src/main/java/com/bank/itarch/
├── model/entity/
│   ├── OssUseStandingBookMain.java      # Main表实体
│   └── OssUseStandingBookDetails.java   # Detail表实体
├── model/dto/
│   ├── OssUseStandingBookMainDTO.java   # Main传输对象
│   ├── OssUseStandingBookDetailsDTO.java # Detail传输对象
│   └── OssUseStandingBookPageQuery.java # 分页查询DTO
├── mapper/
│   ├── OssUseStandingBookMainMapper.java
│   └── OssUseStandingBookDetailsMapper.java
├── service/
│   ├── OssUseStandingBookMainService.java    # 包含实现（无单独Impl文件）
│   └── OssUseStandingBookDetailsService.java
└── controller/
    └── OssUseStandingBookController.java

bank-it-arch-frontend/src/
├── views/oss/StdBook.vue                # 使用台账管理页面
└── api/index.js                         # 添加API方法（修改）

database/
└── migration_v2.7_oss_standing_book.sql # 建表脚本
```

---

## Task 1: 创建数据库迁移脚本

**Files:**
- Create: `database/migration_v2.7_oss_standing_book.sql`

- [ ] **Step 1: 创建迁移脚本**

```sql
-- 开源软件使用台账主表
CREATE TABLE `oss_use_standing_book_main_info` (
  `id`            VARCHAR(64)  NOT NULL COMMENT '主键ID',
  `sw_name`       VARCHAR(256)          COMMENT '开源软件名称',
  `sw_version`    VARCHAR(256)          COMMENT '开源软件版本',
  `sw_full_name`  VARCHAR(512)          COMMENT '开源软件全称',
  `lic_abbr`      VARCHAR(500)          COMMENT '开源许可证集合',
  `environment`   VARCHAR(12)            COMMENT '所属环境: PROD-生产环境; DEVTEST-开发测试环境',
  `scan_date`     DATE                   COMMENT '扫描日期',
  `sw_category`   VARCHAR(32)            COMMENT '软件分类: 开源组件框架/开源基础软件',
  `app_no`        VARCHAR(32)           COMMENT '应用编号',
  `app_name`      VARCHAR(128)          COMMENT '应用全称',
  `sync_datetime` DATETIME              COMMENT '同步时间（两地三中心）',
  `create_mode`   INT(10)      NOT NULL DEFAULT 0 COMMENT '创建方式: 0-本系统创建; 1-异地同步',
  `create_user_id` VARCHAR(32)          COMMENT '创建者',
  `create_datetime` DATETIME            COMMENT '创建时间',
  `update_user_id` VARCHAR(32)          COMMENT '更新者',
  `update_datetime` DATETIME            COMMENT '更新时间',
  `logic_status`  INT(10)      NOT NULL DEFAULT 0 COMMENT '逻辑状态: 0-正常; 1-逻辑删除',
  `is_commerc`    VARCHAR(10)           COMMENT '是否普遍（是否商用）',
  PRIMARY KEY (`id`)
) COMMENT='开源软件使用台账主表';

-- 开源软件使用台账明细表
CREATE TABLE `oss_use_standing_book_details_info` (
  `id`                  VARCHAR(32)  NOT NULL COMMENT '主键ID',
  `sw_name`             VARCHAR(256)          COMMENT '开源软件名称',
  `sw_version`          VARCHAR(256)          COMMENT '开源软件版本',
  `sw_full_name`        VARCHAR(256)          COMMENT '开源软件全称',
  `lic_abbr`            VARCHAR(500)          COMMENT '开源许可证集合',
  `environment`         VARCHAR(12)            COMMENT '所属环境: PROD/DEVTEST',
  `scan_time`           DATETIME              COMMENT '扫描时间',
  `sw_category`         VARCHAR(32)            COMMENT '软件分类',
  `app_no`              VARCHAR(32)           COMMENT '应用编号',
  `app_name`            VARCHAR(128)          COMMENT '应用全称',
  `parent_id`           VARCHAR(64)           COMMENT '父编号(Main表ID)',
  `install_path`        VARCHAR(255)          COMMENT '项目路径/安装路径',
  `source`              INT(10)               COMMENT '台账来源: 0-系统同步; 1-手工上传',
  `ip_or_host_name`     VARCHAR(256)          COMMENT 'IP/主机名称',
  `command`             VARCHAR(256)          COMMENT '软件启动命令',
  `detailed_info`       VARCHAR(256)          COMMENT '版本详细信息',
  `file_type`           VARCHAR(32)            COMMENT '组件文件类型: tgz/jar',
  `depend_type`         VARCHAR(32)            COMMENT '组件依赖类型: maven/npm/go/python',
  `sync_datetime`      DATETIME              COMMENT '同步时间',
  `create_mode`         INT(10)      NOT NULL DEFAULT 0 COMMENT '创建方式: 0-本系统创建; 1-异地同步',
  `create_user_id`      VARCHAR(32)          COMMENT '创建者',
  `create_datetime`     DATETIME              COMMENT '创建时间',
  `update_user_id`      VARCHAR(32)          COMMENT '更新者',
  `update_datetime`     DATETIME              COMMENT '更新时间',
  `logic_status`        INT(10)      NOT NULL DEFAULT 0 COMMENT '逻辑状态: 0-正常; 1-逻辑删除',
  `is_commerc`          VARCHAR(10)           COMMENT '是否普遍',
  `commerc_product_name` VARCHAR(256)          COMMENT '产品名称',
  `commerc_product_version` VARCHAR(256)       COMMENT '产品版本',
  `project_name`        VARCHAR(64)           COMMENT '项目名',
  PRIMARY KEY (`id`)
) COMMENT='开源软件使用台账明细表';
```

- [ ] **Step 2: 创建测试数据SQL（放在脚本底部）**

```sql
-- 测试数据：Main表（开源组件框架示例）
INSERT INTO `oss_use_standing_book_main_info` (`id`, `sw_name`, `sw_version`, `sw_full_name`, `lic_abbr`, `environment`, `scan_date`, `sw_category`, `app_no`, `app_name`, `create_mode`, `logic_status`) VALUES
('MAIN001', 'vue', '3.4.0', 'vue:3.4.0', 'MIT', 'DEVTEST', '2026-03-31', '开源组件框架', 'APP001', '测试应用A', 0, 0),
('MAIN002', 'springboot', '2.7.0', 'springboot:2.7.0', 'Apache-2.0', 'PROD', '2026-03-31', '开源基础软件', 'APP002', '测试应用B', 0, 0);

-- 测试数据：Detail表（开源组件框架的实例）
INSERT INTO `oss_use_standing_book_details_info` (`id`, `sw_name`, `sw_version`, `sw_full_name`, `environment`, `scan_time`, `sw_category`, `app_no`, `app_name`, `parent_id`, `install_path`, `source`, `file_type`, `depend_type`, `project_name`, `create_mode`, `logic_status`) VALUES
('DETAIL001', 'vue', '3.4.0', 'vue:3.4.0', 'DEVTEST', '2026-03-31 10:00:00', '开源组件框架', 'APP001', '测试应用A', 'MAIN001', 'project-a/src/main/vue', 0, 'jar', 'maven', 'ProjectA', 0, 0),
('DETAIL002', 'vue', '3.4.0', 'vue:3.4.0', 'DEVTEST', '2026-03-31 11:00:00', '开源组件框架', 'APP001', '测试应用A', 'MAIN001', 'project-b/pom.xml', 0, 'tgz', 'npm', 'ProjectB', 0, 0);

-- 测试数据：Detail表（开源基础软件的实例）
INSERT INTO `oss_use_standing_book_details_info` (`id`, `sw_name`, `sw_version`, `sw_full_name`, `environment`, `scan_time`, `sw_category`, `app_no`, `app_name`, `parent_id`, `install_path`, `source`, `ip_or_host_name`, `command`, `is_commerc`, `commerc_product_name`, `commerc_product_version`, `create_mode`, `logic_status`) VALUES
('DETAIL003', 'springboot', '2.7.0', 'springboot:2.7.0', 'PROD', '2026-03-31 14:00:00', '开源基础软件', 'APP002', '测试应用B', 'MAIN002', '/opt/app/springboot.jar', 0, '192.168.1.100', 'java -jar springboot.jar', '否', '', '', 0, 0);
```

---

## Task 2: 创建后端实体类

**Files:**
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/entity/OssUseStandingBookMain.java`
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/entity/OssUseStandingBookDetails.java`

- [ ] **Step 1: 创建Main表实体**

```java
package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("oss_use_standing_book_main_info")
public class OssUseStandingBookMain implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 开源软件名称 */
    private String swName;

    /** 开源软件版本 */
    private String swVersion;

    /** 开源软件全称 */
    private String swFullName;

    /** 开源许可证集合 */
    private String licAbbr;

    /** 所属环境: PROD-生产环境; DEVTEST-开发测试环境 */
    private String environment;

    /** 扫描日期 */
    private LocalDate scanDate;

    /** 软件分类: 开源组件框架/开源基础软件 */
    private String swCategory;

    /** 应用编号 */
    private String appNo;

    /** 应用全称 */
    private String appName;

    /** 同步时间（两地三中心） */
    private LocalDateTime syncDatetime;

    /** 创建方式: 0-本系统创建; 1-异地同步 */
    private Integer createMode;

    /** 创建者 */
    private String createUserId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDatetime;

    /** 更新者 */
    private String updateUserId;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDatetime;

    /** 逻辑状态: 0-正常; 1-逻辑删除 */
    @TableLogic
    @TableField(select = false)
    private Integer logicStatus;

    /** 是否普遍（是否商用） */
    private String isCommerc;
}
```

- [ ] **Step 2: 创建Details表实体**

```java
package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oss_use_standing_book_details_info")
public class OssUseStandingBookDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 开源软件名称 */
    private String swName;

    /** 开源软件版本 */
    private String swVersion;

    /** 开源软件全称 */
    private String swFullName;

    /** 开源许可证集合 */
    private String licAbbr;

    /** 所属环境: PROD/DEVTEST */
    private String environment;

    /** 扫描时间 */
    private LocalDateTime scanTime;

    /** 软件分类 */
    private String swCategory;

    /** 应用编号 */
    private String appNo;

    /** 应用全称 */
    private String appName;

    /** 父编号(Main表ID) */
    private String parentId;

    /** 项目路径/安装路径 */
    private String installPath;

    /** 台账来源: 0-系统同步; 1-手工上传 */
    private Integer source;

    /** IP/主机名称 */
    private String ipOrHostName;

    /** 软件启动命令 */
    private String command;

    /** 版本详细信息 */
    private String detailedInfo;

    /** 组件文件类型: tgz/jar */
    private String fileType;

    /** 组件依赖类型: maven/npm/go/python */
    private String dependType;

    /** 同步时间 */
    private LocalDateTime syncDatetime;

    /** 创建方式: 0-本系统创建; 1-异地同步 */
    private Integer createMode;

    /** 创建者 */
    private String createUserId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDatetime;

    /** 更新者 */
    private String updateUserId;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDatetime;

    /** 逻辑状态: 0-正常; 1-逻辑删除 */
    @TableLogic
    @TableField(select = false)
    private Integer logicStatus;

    /** 是否普遍 */
    private String isCommerc;

    /** 产品名称 */
    private String commercProductName;

    /** 产品版本 */
    private String commercProductVersion;

    /** 项目名 */
    private String projectName;
}
```

---

## Task 3: 创建DTO类

**Files:**
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/OssUseStandingBookMainDTO.java`
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/OssUseStandingBookDetailsDTO.java`
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/OssUseStandingBookPageQuery.java`

- [ ] **Step 1: 创建MainDTO**

```java
package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OssUseStandingBookMainDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String swName;
    private String swVersion;
    private String swFullName;
    private String licAbbr;
    private String environment;
    private LocalDate scanDate;
    private String swCategory;
    private String appNo;
    private String appName;
    private LocalDateTime syncDatetime;
    private Integer createMode;
    private String createUserId;
    private LocalDateTime createDatetime;
    private String updateUserId;
    private LocalDateTime updateDatetime;
    private String isCommerc;
}
```

- [ ] **Step 2: 创建DetailsDTO**

```java
package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OssUseStandingBookDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String swName;
    private String swVersion;
    private String swFullName;
    private String licAbbr;
    private String environment;
    private LocalDateTime scanTime;
    private String swCategory;
    private String appNo;
    private String appName;
    private String parentId;
    private String installPath;
    private Integer source;
    private String ipOrHostName;
    private String command;
    private String detailedInfo;
    private String fileType;
    private String dependType;
    private LocalDateTime syncDatetime;
    private Integer createMode;
    private String createUserId;
    private LocalDateTime createDatetime;
    private String updateUserId;
    private LocalDateTime updateDatetime;
    private String isCommerc;
    private String commercProductName;
    private String commercProductVersion;
    private String projectName;
}
```

- [ ] **Step 3: 创建分页查询DTO（继承PageQuery）**

```java
package com.bank.itarch.model.dto;

import com.bank.itarch.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OssUseStandingBookPageQuery extends PageQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String swName;
    private String swVersion;
    private String swCategory;
    private String appName;
    private String environment;
    private String swFullName;
}
```

---

## Task 4: 创建Mapper接口

**Files:**
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/mapper/OssUseStandingBookMainMapper.java`
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/mapper/OssUseStandingBookDetailsMapper.java`

- [ ] **Step 1: 创建MainMapper**

```java
package com.bank.itarch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.model.entity.OssUseStandingBookMain;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OssUseStandingBookMainMapper extends BaseMapper<OssUseStandingBookMain> {
}
```

- [ ] **Step 2: 创建DetailsMapper**

```java
package com.bank.itarch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OssUseStandingBookDetailsMapper extends BaseMapper<OssUseStandingBookDetails> {
}
```

---

## Task 5: 创建Service类

**Files:**
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/service/OssUseStandingBookMainService.java`
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/service/OssUseStandingBookDetailsService.java`

- [ ] **Step 1: 创建MainService**

```java
package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssUseStandingBookMainMapper;
import com.bank.itarch.mapper.OssUseStandingBookDetailsMapper;
import com.bank.itarch.model.dto.OssUseStandingBookMainDTO;
import com.bank.itarch.model.dto.OssUseStandingBookPageQuery;
import com.bank.itarch.model.entity.OssUseStandingBookMain;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OssUseStandingBookMainService extends ServiceImpl<OssUseStandingBookMainMapper, OssUseStandingBookMain> {

    private final OssUseStandingBookDetailsMapper detailsMapper;

    public PageResult<OssUseStandingBookMainDTO> pageQuery(OssUseStandingBookPageQuery query) {
        Page<OssUseStandingBookMain> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<OssUseStandingBookMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssUseStandingBookMain::getSwName, query.getSwName())
               .like(StringUtils.hasText(query.getSwVersion()), OssUseStandingBookMain::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssUseStandingBookMain::getSwCategory, query.getSwCategory())
               .like(StringUtils.hasText(query.getAppName()), OssUseStandingBookMain::getAppName, query.getAppName())
               .eq(StringUtils.hasText(query.getEnvironment()), OssUseStandingBookMain::getEnvironment, query.getEnvironment())
               .like(StringUtils.hasText(query.getSwFullName()), OssUseStandingBookMain::getSwFullName, query.getSwFullName())
               .orderByDesc(OssUseStandingBookMain::getCreateDatetime);

        IPage<OssUseStandingBookMain> result = page(page, wrapper);

        List<OssUseStandingBookMainDTO> dtoList = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssUseStandingBookMainDTO getById(String id) {
        OssUseStandingBookMain main = super.getById(id);
        if (main == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        return toDTO(main);
    }

    @Transactional
    public OssUseStandingBookMainDTO create(OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain main = toEntity(dto);
        main.setCreateMode(0);
        save(main);
        return toDTO(main);
    }

    @Transactional
    public OssUseStandingBookMainDTO update(String id, OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        updateEntity(existing, dto);
        updateById(existing);

        // 同步更新关联的Detail冗余字段
        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, id);
        List<OssUseStandingBookDetails> details = detailsMapper.selectList(wrapper);
        for (OssUseStandingBookDetails detail : details) {
            syncDetailFromMain(detail, existing);
            detailsMapper.updateById(detail);
        }

        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssUseStandingBookMain existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        // 逻辑删除关联的Detail
        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, id);
        List<OssUseStandingBookDetails> details = detailsMapper.selectList(wrapper);
        for (OssUseStandingBookDetails detail : details) {
            detail.setLogicStatus(1);
            detailsMapper.updateById(detail);
        }
        removeById(id);
    }

    public List<OssUseStandingBookMainDTO> listAllForExport(OssUseStandingBookPageQuery query) {
        LambdaQueryWrapper<OssUseStandingBookMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssUseStandingBookMain::getSwName, query.getSwName())
               .like(StringUtils.hasText(query.getSwVersion()), OssUseStandingBookMain::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssUseStandingBookMain::getSwCategory, query.getSwCategory())
               .like(StringUtils.hasText(query.getAppName()), OssUseStandingBookMain::getAppName, query.getAppName())
               .eq(StringUtils.hasText(query.getEnvironment()), OssUseStandingBookMain::getEnvironment, query.getEnvironment())
               .orderByDesc(OssUseStandingBookMain::getCreateDatetime);

        List<OssUseStandingBookMain> list = list(wrapper);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private void syncDetailFromMain(OssUseStandingBookDetails detail, OssUseStandingBookMain main) {
        detail.setSwName(main.getSwName());
        detail.setSwVersion(main.getSwVersion());
        detail.setSwFullName(main.getSwFullName());
        detail.setLicAbbr(main.getLicAbbr());
        detail.setEnvironment(main.getEnvironment());
        detail.setSwCategory(main.getSwCategory());
        detail.setAppNo(main.getAppNo());
        detail.setAppName(main.getAppName());
        detail.setIsCommerc(main.getIsCommerc());
    }

    private OssUseStandingBookMainDTO toDTO(OssUseStandingBookMain main) {
        OssUseStandingBookMainDTO dto = new OssUseStandingBookMainDTO();
        dto.setId(main.getId());
        dto.setSwName(main.getSwName());
        dto.setSwVersion(main.getSwVersion());
        dto.setSwFullName(main.getSwFullName());
        dto.setLicAbbr(main.getLicAbbr());
        dto.setEnvironment(main.getEnvironment());
        dto.setScanDate(main.getScanDate());
        dto.setSwCategory(main.getSwCategory());
        dto.setAppNo(main.getAppNo());
        dto.setAppName(main.getAppName());
        dto.setSyncDatetime(main.getSyncDatetime());
        dto.setCreateMode(main.getCreateMode());
        dto.setCreateUserId(main.getCreateUserId());
        dto.setCreateDatetime(main.getCreateDatetime());
        dto.setUpdateUserId(main.getUpdateUserId());
        dto.setUpdateDatetime(main.getUpdateDatetime());
        dto.setIsCommerc(main.getIsCommerc());
        return dto;
    }

    private OssUseStandingBookMain toEntity(OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain main = new OssUseStandingBookMain();
        main.setSwName(dto.getSwName());
        main.setSwVersion(dto.getSwVersion());
        main.setSwFullName(dto.getSwFullName());
        main.setLicAbbr(dto.getLicAbbr());
        main.setEnvironment(dto.getEnvironment());
        main.setScanDate(dto.getScanDate());
        main.setSwCategory(dto.getSwCategory());
        main.setAppNo(dto.getAppNo());
        main.setAppName(dto.getAppName());
        main.setSyncDatetime(dto.getSyncDatetime());
        main.setCreateMode(dto.getCreateMode());
        main.setCreateUserId(dto.getCreateUserId());
        main.setUpdateUserId(dto.getUpdateUserId());
        main.setIsCommerc(dto.getIsCommerc());
        return main;
    }

    private void updateEntity(OssUseStandingBookMain main, OssUseStandingBookMainDTO dto) {
        if (dto.getSwName() != null) main.setSwName(dto.getSwName());
        if (dto.getSwVersion() != null) main.setSwVersion(dto.getSwVersion());
        if (dto.getSwFullName() != null) main.setSwFullName(dto.getSwFullName());
        if (dto.getLicAbbr() != null) main.setLicAbbr(dto.getLicAbbr());
        if (dto.getEnvironment() != null) main.setEnvironment(dto.getEnvironment());
        if (dto.getScanDate() != null) main.setScanDate(dto.getScanDate());
        if (dto.getSwCategory() != null) main.setSwCategory(dto.getSwCategory());
        if (dto.getAppNo() != null) main.setAppNo(dto.getAppNo());
        if (dto.getAppName() != null) main.setAppName(dto.getAppName());
        if (dto.getSyncDatetime() != null) main.setSyncDatetime(dto.getSyncDatetime());
        if (dto.getUpdateUserId() != null) main.setUpdateUserId(dto.getUpdateUserId());
        if (dto.getIsCommerc() != null) main.setIsCommerc(dto.getIsCommerc());
    }
}
```

- [ ] **Step 2: 创建DetailsService**

```java
package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssUseStandingBookDetailsMapper;
import com.bank.itarch.model.dto.OssUseStandingBookDetailsDTO;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OssUseStandingBookDetailsService extends ServiceImpl<OssUseStandingBookDetailsMapper, OssUseStandingBookDetails> {

    public PageResult<OssUseStandingBookDetailsDTO> pageQueryByParentId(String parentId, Integer page, Integer pageSize) {
        Page<OssUseStandingBookDetails> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, parentId)
               .orderByDesc(OssUseStandingBookDetails::getCreateDatetime);

        IPage<OssUseStandingBookDetails> result = page(pageParam, wrapper);

        List<OssUseStandingBookDetailsDTO> dtoList = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), page, pageSize);
    }

    public OssUseStandingBookDetailsDTO getById(String id) {
        OssUseStandingBookDetails detail = super.getById(id);
        if (detail == null) {
            throw new BusinessException(2001, "Detail record not found");
        }
        return toDTO(detail);
    }

    @Transactional
    public OssUseStandingBookDetailsDTO create(OssUseStandingBookDetailsDTO dto) {
        OssUseStandingBookDetails detail = toEntity(dto);
        detail.setCreateMode(0);
        save(detail);
        return toDTO(detail);
    }

    @Transactional
    public OssUseStandingBookDetailsDTO update(String id, OssUseStandingBookDetailsDTO dto) {
        OssUseStandingBookDetails existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Detail record not found");
        }
        updateEntity(existing, dto);
        updateById(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssUseStandingBookDetails existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Detail record not found");
        }
        removeById(id);
    }

    private OssUseStandingBookDetailsDTO toDTO(OssUseStandingBookDetails detail) {
        OssUseStandingBookDetailsDTO dto = new OssUseStandingBookDetailsDTO();
        dto.setId(detail.getId());
        dto.setSwName(detail.getSwName());
        dto.setSwVersion(detail.getSwVersion());
        dto.setSwFullName(detail.getSwFullName());
        dto.setLicAbbr(detail.getLicAbbr());
        dto.setEnvironment(detail.getEnvironment());
        dto.setScanTime(detail.getScanTime());
        dto.setSwCategory(detail.getSwCategory());
        dto.setAppNo(detail.getAppNo());
        dto.setAppName(detail.getAppName());
        dto.setParentId(detail.getParentId());
        dto.setInstallPath(detail.getInstallPath());
        dto.setSource(detail.getSource());
        dto.setIpOrHostName(detail.getIpOrHostName());
        dto.setCommand(detail.getCommand());
        dto.setDetailedInfo(detail.getDetailedInfo());
        dto.setFileType(detail.getFileType());
        dto.setDependType(detail.getDependType());
        dto.setSyncDatetime(detail.getSyncDatetime());
        dto.setCreateMode(detail.getCreateMode());
        dto.setCreateUserId(detail.getCreateUserId());
        dto.setCreateDatetime(detail.getCreateDatetime());
        dto.setUpdateUserId(detail.getUpdateUserId());
        dto.setUpdateDatetime(detail.getUpdateDatetime());
        dto.setIsCommerc(detail.getIsCommerc());
        dto.setCommercProductName(detail.getCommercProductName());
        dto.setCommercProductVersion(detail.getCommercProductVersion());
        dto.setProjectName(detail.getProjectName());
        return dto;
    }

    private OssUseStandingBookDetails toEntity(OssUseStandingBookDetailsDTO dto) {
        OssUseStandingBookDetails detail = new OssUseStandingBookDetails();
        detail.setSwName(dto.getSwName());
        detail.setSwVersion(dto.getSwVersion());
        detail.setSwFullName(dto.getSwFullName());
        detail.setLicAbbr(dto.getLicAbbr());
        detail.setEnvironment(dto.getEnvironment());
        detail.setScanTime(dto.getScanTime());
        detail.setSwCategory(dto.getSwCategory());
        detail.setAppNo(dto.getAppNo());
        detail.setAppName(dto.getAppName());
        detail.setParentId(dto.getParentId());
        detail.setInstallPath(dto.getInstallPath());
        detail.setSource(dto.getSource());
        detail.setIpOrHostName(dto.getIpOrHostName());
        detail.setCommand(dto.getCommand());
        detail.setDetailedInfo(dto.getDetailedInfo());
        detail.setFileType(dto.getFileType());
        detail.setDependType(dto.getDependType());
        detail.setSyncDatetime(dto.getSyncDatetime());
        detail.setCreateUserId(dto.getCreateUserId());
        detail.setUpdateUserId(dto.getUpdateUserId());
        detail.setIsCommerc(dto.getIsCommerc());
        detail.setCommercProductName(dto.getCommercProductName());
        detail.setCommercProductVersion(dto.getCommercProductVersion());
        detail.setProjectName(dto.getProjectName());
        return detail;
    }

    private void updateEntity(OssUseStandingBookDetails detail, OssUseStandingBookDetailsDTO dto) {
        if (dto.getSwName() != null) detail.setSwName(dto.getSwName());
        if (dto.getSwVersion() != null) detail.setSwVersion(dto.getSwVersion());
        if (dto.getSwFullName() != null) detail.setSwFullName(dto.getSwFullName());
        if (dto.getLicAbbr() != null) detail.setLicAbbr(dto.getLicAbbr());
        if (dto.getEnvironment() != null) detail.setEnvironment(dto.getEnvironment());
        if (dto.getScanTime() != null) detail.setScanTime(dto.getScanTime());
        if (dto.getSwCategory() != null) detail.setSwCategory(dto.getSwCategory());
        if (dto.getAppNo() != null) detail.setAppNo(dto.getAppNo());
        if (dto.getAppName() != null) detail.setAppName(dto.getAppName());
        if (dto.getInstallPath() != null) detail.setInstallPath(dto.getInstallPath());
        if (dto.getSource() != null) detail.setSource(dto.getSource());
        if (dto.getIpOrHostName() != null) detail.setIpOrHostName(dto.getIpOrHostName());
        if (dto.getCommand() != null) detail.setCommand(dto.getCommand());
        if (dto.getDetailedInfo() != null) detail.setDetailedInfo(dto.getDetailedInfo());
        if (dto.getFileType() != null) detail.setFileType(dto.getFileType());
        if (dto.getDependType() != null) detail.setDependType(dto.getDependType());
        if (dto.getSyncDatetime() != null) detail.setSyncDatetime(dto.getSyncDatetime());
        if (dto.getUpdateUserId() != null) detail.setUpdateUserId(dto.getUpdateUserId());
        if (dto.getIsCommerc() != null) detail.setIsCommerc(dto.getIsCommerc());
        if (dto.getCommercProductName() != null) detail.setCommercProductName(dto.getCommercProductName());
        if (dto.getCommercProductVersion() != null) detail.setCommercProductVersion(dto.getCommercProductVersion());
        if (dto.getProjectName() != null) detail.setProjectName(dto.getProjectName());
    }
}
```

---

## Task 6: 创建Controller

**Files:**
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/controller/OssUseStandingBookController.java`

- [ ] **Step 1: 创建Controller**

```java
package com.bank.itarch.controller;

import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.dto.OssUseStandingBookDetailsDTO;
import com.bank.itarch.model.dto.OssUseStandingBookMainDTO;
import com.bank.itarch.model.dto.OssUseStandingBookPageQuery;
import com.bank.itarch.service.OssUseStandingBookDetailsService;
import com.bank.itarch.service.OssUseStandingBookMainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/oss/standing-book")
@RequiredArgsConstructor
@Tag(name = "使用台账管理")
public class OssUseStandingBookController {

    private final OssUseStandingBookMainService mainService;
    private final OssUseStandingBookDetailsService detailsService;

    // ==================== Main表接口 ====================

    @GetMapping("/main/list")
    @Operation(summary = "台账主列表")
    public Result<PageResult<OssUseStandingBookMainDTO>> listMain(OssUseStandingBookPageQuery query) {
        return Result.success(mainService.pageQuery(query));
    }

    @GetMapping("/main/{id}")
    @Operation(summary = "台账主详情")
    public Result<OssUseStandingBookMainDTO> getMainById(@PathVariable String id) {
        return Result.success(mainService.getById(id));
    }

    @PostMapping("/main")
    @Operation(summary = "创建台账主记录")
    public Result<OssUseStandingBookMainDTO> createMain(@RequestBody OssUseStandingBookMainDTO dto) {
        return Result.success("创建成功", mainService.create(dto));
    }

    @PutMapping("/main/{id}")
    @Operation(summary = "更新台账主记录")
    public Result<OssUseStandingBookMainDTO> updateMain(@PathVariable String id, @RequestBody OssUseStandingBookMainDTO dto) {
        return Result.success("更新成功", mainService.update(id, dto));
    }

    @DeleteMapping("/main/{id}")
    @Operation(summary = "删除台账主记录")
    public Result<Void> deleteMain(@PathVariable String id) {
        mainService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/main/export")
    @Operation(summary = "导出台账主列表")
    public Result<List<OssUseStandingBookMainDTO>> exportMain(OssUseStandingBookPageQuery query) {
        return Result.success(mainService.listAllForExport(query));
    }

    // ==================== Detail表接口 ====================

    @GetMapping("/details")
    @Operation(summary = "台账明细列表（按parentId）")
    public Result<PageResult<OssUseStandingBookDetailsDTO>> listDetails(
            @RequestParam String parentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(detailsService.pageQueryByParentId(parentId, page, pageSize));
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "台账明细详情")
    public Result<OssUseStandingBookDetailsDTO> getDetailById(@PathVariable String id) {
        return Result.success(detailsService.getById(id));
    }

    @PostMapping("/details")
    @Operation(summary = "创建台账明细记录")
    public Result<OssUseStandingBookDetailsDTO> createDetail(@RequestBody OssUseStandingBookDetailsDTO dto) {
        return Result.success("创建成功", detailsService.create(dto));
    }

    @PutMapping("/details/{id}")
    @Operation(summary = "更新台账明细记录")
    public Result<OssUseStandingBookDetailsDTO> updateDetail(@PathVariable String id, @RequestBody OssUseStandingBookDetailsDTO dto) {
        return Result.success("更新成功", detailsService.update(id, dto));
    }

    @DeleteMapping("/details/{id}")
    @Operation(summary = "删除台账明细记录")
    public Result<Void> deleteDetail(@PathVariable String id) {
        detailsService.delete(id);
        return Result.success("删除成功", null);
    }
}
```

---

## Task 7: 前端 - 添加API方法

**Files:**
- Modify: `bank-it-arch-frontend/src/api/index.js`

- [ ] **Step 1: 添加API方法**

在 `export const ossSoftwareApi` 之后添加：

```javascript
export const ossUseStandingBookApi = {
  // Main表
  listMain: (params) => api.get('/oss/standing-book/main/list', { params }),
  getMainById: (id) => api.get(`/oss/standing-book/main/${id}`),
  createMain: (data) => api.post('/oss/standing-book/main', data),
  updateMain: (id, data) => api.put(`/oss/standing-book/main/${id}`, data),
  deleteMain: (id) => api.delete(`/oss/standing-book/main/${id}`),
  exportMain: (params) => api.get('/oss/standing-book/main/export', { params }),
  // Detail表
  listDetails: (params) => api.get('/oss/standing-book/details', { params }),
  getDetailById: (id) => api.get(`/oss/standing-book/details/${id}`),
  createDetail: (data) => api.post('/oss/standing-book/details', data),
  updateDetail: (id, data) => api.put(`/oss/standing-book/details/${id}`, data),
  deleteDetail: (id) => api.delete(`/oss/standing-book/details/${id}`)
}
```

---

## Task 8: 前端 - 添加路由

**Files:**
- Modify: `bank-it-arch-frontend/src/router/index.js`

- [ ] **Step 1: 添加路由**

在现有的OSS路由附近添加：

```javascript
{
  path: '/oss',
  component: Layout,
  meta: { title: '开源软件资产管理' },
  children: [
    // ... existing routes ...
    {
      path: 'stdBook',
      name: 'OssStdBook',
      component: () => import('@/views/oss/StdBook.vue'),
      meta: { title: '使用台账管理' }
    }
  ]
}
```

---

## Task 9: 前端 - 创建StdBook.vue页面

**Files:**
- Create: `bank-it-arch-frontend/src/views/oss/StdBook.vue`

- [ ] **Step 1: 创建页面组件**

```vue
<template>
  <div class="standing-book-container">
    <!-- 查询区域 -->
    <el-collapse v-model="searchVisible">
      <el-collapse-item title="查询条件" name="search">
        <el-form :inline="true" :model="queryForm" class="search-form">
          <el-form-item label="开源软件名称">
            <el-input v-model="queryForm.swName" placeholder="请输入开源软件名称" clearable />
          </el-form-item>
          <el-form-item label="开源软件版本">
            <el-input v-model="queryForm.swVersion" placeholder="请输入开源软件版本" clearable />
          </el-form-item>
          <el-form-item label="软件分类">
            <el-select v-model="queryForm.swCategory" placeholder="请选择" clearable>
              <el-option label="开源组件框架" value="开源组件框架" />
              <el-option label="开源基础软件" value="开源基础软件" />
            </el-select>
          </el-form-item>
          <el-form-item label="应用名称">
            <el-input v-model="queryForm.appName" placeholder="请输入应用名称" clearable />
          </el-form-item>
          <el-form-item label="所属环境">
            <el-select v-model="queryForm.environment" placeholder="请选择" clearable>
              <el-option label="生产环境" value="PROD" />
              <el-option label="开发测试环境" value="DEVTEST" />
            </el-select>
          </el-form-item>
          <el-form-item label="开源软件全称">
            <el-input v-model="queryForm.swFullName" placeholder="请输入开源软件全称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="default" @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-item>
    </el-collapse>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary">手工同步</el-button>
      <el-button type="warning">导出</el-button>
      <el-button type="danger">导入</el-button>
    </div>

    <!-- 列表区域 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="swName" label="开源软件名称" min-width="120" />
      <el-table-column prop="swVersion" label="开源软件版本" width="120" />
      <el-table-column prop="swFullName" label="开源软件全称" min-width="180" />
      <el-table-column prop="swCategory" label="软件分类" width="120" />
      <el-table-column prop="licAbbr" label="开源许可证" width="120" />
      <el-table-column prop="environment" label="所属环境" width="120">
        <template #default="{ row }">
          {{ row.environment === 'PROD' ? '生产环境' : '开发测试环境' }}
        </template>
      </el-table-column>
      <el-table-column prop="appName" label="应用名称" min-width="150" />
      <el-table-column prop="scanDate" label="扫描时间" width="120" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadData"
      @current-change="loadData"
    />

    <!-- 详情弹窗 -->
    <el-dialog
      :title="detailDialogTitle"
      v-model="detailVisible"
      width="900px"
      :close-on-click-modal="false"
    >
      <!-- 开源组件框架的详情字段 -->
      <el-table v-if="currentMain.swCategory === '开源组件框架'" :data="detailData" border stripe>
        <el-table-column prop="installPath" label="项目路径" min-width="200" />
        <el-table-column prop="scanTime" label="扫描时间" width="160" />
        <el-table-column prop="projectName" label="项目" width="120" />
        <el-table-column prop="fileType" label="文件类型" width="100" />
        <el-table-column prop="dependType" label="依赖类型" width="100" />
      </el-table>

      <!-- 开源基础软件的详情字段 -->
      <el-table v-else :data="detailData" border stripe>
        <el-table-column prop="installPath" label="项目路径" min-width="200" />
        <el-table-column prop="scanTime" label="扫描时间" width="160" />
        <el-table-column prop="ipOrHostName" label="IP/主机名称" min-width="150" />
        <el-table-column prop="command" label="启动命令" min-width="200" />
        <el-table-column prop="isCommerc" label="是否第三方产品" width="120">
          <template #default="{ row }">
            {{ row.isCommerc === '是' ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="commercProductName" label="第三方产品名称" width="150" />
        <el-table-column prop="commercProductVersion" label="第三方产品版本" width="150" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="detailPagination.page"
        v-model:page-size="detailPagination.pageSize"
        :total="detailPagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadDetails"
        @current-change="loadDetails"
        style="margin-top: 16px; justify-content: flex-end;"
      />

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ossUseStandingBookApi } from '@/api'
import { ElMessage } from 'element-plus'

const searchVisible = ref(true)
const loading = ref(false)
const detailVisible = ref(false)
const detailDialogTitle = ref('使用详情')

const queryForm = reactive({
  swName: '',
  swVersion: '',
  swCategory: '',
  appName: '',
  environment: '',
  swFullName: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const tableData = ref([])
const currentMain = ref({})
const detailData = ref([])

const detailPagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

onMounted(() => {
  loadData()
})

function handleReset() {
  Object.assign(queryForm, {
    swName: '',
    swVersion: '',
    swCategory: '',
    appName: '',
    environment: '',
    swFullName: ''
  })
  pagination.page = 1
  loadData()
}

function handleQuery() {
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    const res = await ossUseStandingBookApi.listMain(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

async function handleDetail(row) {
  currentMain.value = row
  detailDialogTitle.value = '使用详情'
  detailVisible.value = true
  detailPagination.page = 1
  await loadDetails()
}

async function loadDetails() {
  try {
    const params = {
      parentId: currentMain.value.id,
      page: detailPagination.page,
      pageSize: detailPagination.pageSize
    }
    const res = await ossUseStandingBookApi.listDetails(params)
    detailData.value = res.data.records || []
    detailPagination.total = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}
</script>

<style scoped>
.standing-book-container {
  padding: 16px;
}

.search-form {
  padding: 0 16px;
}

.toolbar {
  padding: 12px 16px;
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
```

---

## Task 10: 验证与自测

- [ ] **Step 1: 后端编译**

Run: `cd bank-it-arch-backend && mvn compile`
Expected: BUILD SUCCESS，无报错

- [ ] **Step 2: 前端编译**

Run: `cd bank-it-arch-frontend && npm run build`
Expected: 无报错，构建成功

- [ ] **Step 3: 执行数据库脚本**

在MySQL中执行 `migration_v2.7_oss_standing_book.sql` 中的建表语句和测试数据SQL

- [ ] **Step 4: 功能自测**

1. 启动后端 `mvn spring-boot:run`
2. 启动前端 `npm run dev`
3. 访问 `/oss/stdBook` 页面
4. 验证列表加载：Main表数据能正常显示
5. 验证详情弹窗：点击「详情」能弹出弹窗
   - sw_category=开源组件框架 时显示：项目路径、扫描时间、项目、文件类型、依赖类型
   - sw_category=开源基础软件 时显示：项目路径、扫描时间、IP/主机名称、启动命令、是否第三方产品、第三方产品名称、第三方产品版本
6. 验证查询：输入查询条件点击「查询」，能正确筛选数据

---

## 自检清单

- [ ] Main表实体字段与数据库表结构一致
- [ ] Details表实体字段与数据库表结构一致
- [ ] Controller接口路径为 `/v1/oss/standing-book/main/*` 和 `/v1/oss/standing-book/details/*`
- [ ] 前端API方法名与后端接口一一对应
- [ ] 详情弹窗根据sw_category正确切换显示字段
- [ ] Detail查询使用parentId过滤
- [ ] 测试数据已插入且可正常查询
