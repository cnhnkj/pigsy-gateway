# 背景
惠农网之前的网关是基于zuul来进行开发的，zuul在市场上逐步被gateway所代替，异步模型更好的支持高并发场景，在替换的同时解决了一些遗留问题。

# 架构
![avatar](./doc/架构图.png)

# 依赖

- `Spring-Boot`的`2.3.9.RELEASE`版本
- `Spring-Cloud`的`Hoxton.SR10`版本
- `Apollo`的`1.8.1`版本
- `Sentry`的`21.3.0`版本
- `Redis`的`5.0.8`版本

# 模块

- `pigsy-gateway-eureka` 提供了核心的服务注册和发现功能，网关通过先从`eureka`模块获取数据，然后进行路由选择到具体的服务
- `pigsy-gateway-apollo` 提供了配置中心功能，网关通过先从`apollo`模块实时获取配置数据，并且进行动态更新
- `pigsy-gateway-auth` 提供了各种环境的权限认证的方式，可能认证要处理不同的业务逻辑，故独立成独立服务进行验证，把认证结果告知网关
- `pigsy-gateway-sentry` 提供了错误的报警和统计展示功能
- `pigsy-gateway-core` 提供了网关的4个模块的核心功能


# 功能结构图


# 功能特点


# 关于
