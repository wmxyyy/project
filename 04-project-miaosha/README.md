## 基于SpringBoot的仿高并发秒杀商城

### 第一章 项目框架搭建

#### 1.1 Spring Boot环境搭建

#### 1.1 集成Thymeleaf，Result结果封装

#### 1.3 集成Mybatis+Druid

#### 1.4 集成Jedis+Redis安装+通过缓存Key封装



### 第二章 实现登录功能

#### 2.1 数据库设计

#### 2.2 明文密码两次MD5处理

- 客户端：PASS = MD5（明文 + 固定Salt）
- 服务端：PASS = MD5（用户输入 + 随机Salt）

#### 2.3 JSR303参数校验+全局异常处理器

#### 2.4 分布式Session

### 第三章 实现秒杀功能

#### 3.1 数据库设计

- 商品表
- 秒杀商品表
- 订单表
- 秒杀订单表

#### 3.2 商品列表页

#### 3.3 商品详情页

#### 3.4 订单详情页



### 第四章 JMeter压测

#### 4.1 JMeter入门

#### 4.2 自定义变量模拟多用户

#### 4.3 JMete命令行使用

#### 4.4 Redis压测工具redis-benchmark

#### 4.5 Spring Boot打war包

### 第五章 页面优化技术

#### 5.1 页面缓存+URL缓存+对象缓存

#### 5.2 页面静态化，前后端分离

- 常用技术AngularJS，Vue.js
- 优点：利用浏览器的缓存

#### 5.3 静态资源优化

- JS/CSS压缩，减少流量

- 多个JS/CSS组合，减少连接数

- CDN就近访问

  

### 第六章 接口优化

#### 6.1 Redis预减库存减少数据库访问

- ##### 思路：减少数据库访问

  1. 系统初始化，把商品库存数量加载到redis
  2. 收到请求，redis预减库存，库存不足，直接返回，否则进入3
  3. 请求入队，立即返回排队中
  4. 请求出队，生成订单，减少库存
  5. 客户端轮询，是否秒杀成功

#### 6.2 内存标记减少Redis访问

#### 6.3 RabbitMQ队列缓冲，异步下单，增强用户体验

- 添加依赖spring-boot-start-amqp
- 创建消息接收者
- 创建消息发送者

#### 6.4 RabbitMQ安装和Spring Boot集成

#### 6.5 访问Nginx水平扩展

#### 6.6 压测



### 第七章 安全优化

#### 7.1 秒杀接口地址隐藏

- ##### 思路：秒杀开始之前，先去请求接口获取秒杀地址

  1. 接口改造，带上PathVariable参数
  2. 添加生成地址的接口
  3. 秒杀收到请求，先验证PathVariable

#### 7.2 数学公式验证码

- ##### 思路：点击秒杀之前，先输入验证码，分散用户的请求

  1. 添加生成验证码的接口
  2. 在获取秒杀路径的时候，验证验证码
  3. ScriptEngine使用

#### 7.3 接口防刷

- #####  思路：对接口做限流