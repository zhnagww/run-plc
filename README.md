# Run-PLC 项目说明文档

## 项目简介

Run-PLC 是一个基于 Spring Boot 和 Vue 3 的工业自动化控制系统，主要用于管理和调试 PLC（可编程逻辑控制器）设备。该项目提供了完整的 PLC 信息管理、点位配置、在线调试等功能，方便工程师对工业现场的 PLC 设备进行远程监控和控制。

## 功能特性

### 1. PLC信息管理
- 添加、编辑、删除PLC设备信息（IP地址和端口号）
- Excel批量导入PLC及点位信息
- PLC设备列表展示与管理

### 2. PLC点位管理
- 为每个PLC设备配置点位信息
- 支持读取型点位和写入型点位配置
- 点位名称、编号等详细信息管理

### 3. 在线调试功能
- 实时读取PLC点位数值
- 向PLC点位写入指定值
- 组合调试功能（用于复杂的PLC控制序列）

### 4. 数据持久化
- 使用SQLite数据库存储PLC配置信息
- 自动初始化数据库表结构

## 技术架构

### 后端技术栈
- **Spring Boot 2.7.18**: 主框架，提供Web服务和IoC容器
- **MyBatis**: 数据库访问框架
- **SQLite**: 轻量级关系型数据库
- **Modbus4J**: Modbus协议通信库，用于与PLC设备通信
- **Swagger**: API文档生成工具
- **Maven**: 项目构建和依赖管理工具

### 前端技术栈
- **Vue 3**: 渐进式JavaScript框架
- **TypeScript**: JavaScript的超集，提供类型安全
- **Element Plus**: Vue 3组件库
- **Vite**: 前端构建工具
- **Axios**: HTTP客户端
- **Pinia**: Vue状态管理库
- **Vue Router**: Vue路由管理

## 项目结构

```
run-plc/
├── frontend/           # 前端项目源码
│   ├── src/
│   │   ├── api/        # API接口封装
│   │   ├── pages/      # 页面组件
│   │   ├── layouts/    # 布局组件
│   │   └── ...
│   ├── vite.config.ts  # Vite配置文件
│   └── package.json    # 前端依赖配置
├── src/                # 后端Java源码
│   ├── main/
│   │   ├── java/
│   │   │   └── com/runplc/
│   │   │       ├── controller/  # 控制层
│   │   │       ├── service/     # 业务层
│   │   │       ├── entity/      # 实体类
│   │   │       ├── mapper/      # 数据访问层
│   │   │       ├── modbus/      # Modbus通信模块
│   │   │       └── ...
│   │   └── resources/
│   │       ├── mapper/          # MyBatis映射文件
│   │       ├── static/          # 静态资源文件
│   │       └── application.yml  # 应用配置文件
├── pom.xml             # Maven配置文件
└── runplc.db           # SQLite数据库文件
```

## 本地运行方式

### 环境要求
- JDK 8 或更高版本
- Node.js 16 或更高版本
- Maven 3.6 或更高版本
- npm 或 yarn 包管理器

### 后端运行步骤

1. 使用IDE导入项目或在命令行中进入项目根目录
2. 执行Maven命令构建项目：
   ```bash
   mvn clean install
   ```
3. 运行Spring Boot应用：
   ```bash
   mvn spring-boot:run
   ```
   或者直接运行生成的jar包：
   ```bash
   java -jar target/run-plc-1.0.jar
   ```

4. 后端服务将在 `http://localhost:8080` 启动

### 前端运行步骤

1. 进入前端目录：
   ```bash
   cd frontend
   ```
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```
4. 前端开发服务器将在 `http://localhost:5173` 启动，并自动代理API请求到后端

### 访问应用

在开发环境中，可以通过以下地址访问应用：

- 前端开发环境: http://localhost:5173
- 直接访问后端: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui/index.html

## 部署方式

### 后端部署

1. 构建可执行jar包：
   ```bash
   mvn clean package
   ```
2. 将生成的 `target/run-plc-1.0.jar` 文件复制到目标服务器
3. 运行应用：
   ```bash
   java -jar run-plc-1.0.jar
   ```

### 前端部署

1. 构建生产版本：
   ```bash
   cd frontend
   npm run build
   ```
2. 构建产物将输出到 `src/main/resources/static/vue` 目录，这是由[vite.config.ts](file:///D:/work/code/run-plc/frontend/vite.config.ts)中配置决定的
3. 构建完成后，重启后端服务即可通过 `http://服务器IP:8080/vue/index.html` 访问前端页面

### 注意事项

1. 部署时确保服务器防火墙开放相应端口（默认8080）
2. 如果需要更改端口或其他配置，请修改[application.yml](file:///D:/work/code/run-plc/src/main/resources/application.yml)文件
3. 数据库文件(runplc.db)会自动在项目根目录创建，确保应用有写入权限

## API接口文档

项目集成了Swagger，可通过 `http://服务器地址:端口/swagger-ui/index.html` 查看和测试API接口。