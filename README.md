# *背景*
惠农网之前的网关是基于zuul来进行开发的，zuul在市场上逐步被gateway所代替，异步模型更好的支持高并发场景，在替换的同时解决了一些遗留问题。

# *架构*
![avatar](./docs/架构图.png)

# *依赖*

- `Spring-Boot`的`2.3.9.RELEASE`版本
- `Spring-Cloud`的`Hoxton.SR10`版本
- `Apollo`的`1.8.1`版本
- `Sentry`的`21.3.0`版本
- `Redis`的`5.0.8`版本

# *模块*

- `pigsy-gateway-eureka` 提供了核心的服务注册和发现功能，网关通过先从`eureka`模块获取数据，然后进行路由选择到具体的服务
- `pigsy-gateway-apollo` 提供了配置中心功能，网关通过先从`apollo`模块实时获取配置数据，并且进行动态更新
- `pigsy-gateway-auth` 提供了各种环境的权限认证的方式，可能认证要处理不同的业务逻辑，故独立成独立服务进行验证，把认证结果告知网关
- `pigsy-gateway-sentry` 提供了错误的报警和统计展示功能
- `pigsy-gateway-redis` 提供了缓存的功能
- `pigsy-gateway-core` 提供了网关的4个模块的核心功能
- `pigsy-gateway-common` 提供了一些公共的基础类和基础方法，方便整个项目进行使用


# *功能约定*

### *请求路径约定*

- 对外提供服务接口是 `/{service}/api/**` 开头的，其中`{service}`是要提供服务的具体服务名称
- 对外提供服务接口一定需要进行鉴权是 `/{service}/api/auth/**` 开头的，其中`{service}`是要提供服务的具体服务名称
- 对外提供服务接口如果存在token，则需要鉴权是 `/{service}/api/transform/**` 开头的，其中`{service}`是要提供服务的具体服务名称



# *过滤器功能特点*

### *网关全局过滤器*

- `CorsFilter` 这个全局过滤器在网关层面提供了统一的跨域能力。
  另外补充遇到的一个坑：<font color=red>由于可能部分后端服务单独设置了跨域的头，
  所以在配置这个跨域过滤器的时候，要加入对应的`DedupeResponseHeader`参数来解决这个问题</font>
- `GuardFilter` 这个全局过滤器给网关提供了部分保护能力。
  主要包括（ip黑名单、对应部分服务停机、对应部分接口停机、header黑名单等功能）
- `AccessLogFilter` 这个全局过滤器用于记录每次请求访问的部分关键参数。
  同时对于错误请求以及慢请求则打印更加详细的信息

### *网关普通过滤器*

#### api类的主要过滤器

- `ApiGatewayFilterFactory` 这个过滤器用于判断所访问的接口是否是对外接口，如果非对外接口则直接返回`404`状态。
- `ParamCheckGatewayFilterFactory` 这个过滤器用于检查所传入的统一参数有效性验证，解析并且转换部分标准参数，
  同时也做了接口的`防重放验证`、`签名验证`、`客户端时间验证`。目的是提高接口的完全性。
- `AuthGatewayFilterFactory` 这个过滤器提供了必须登录的验证。主要逻辑是根据访问的服务路径来对`token`进行验证。
  如果验证成功则把相关的信息一起传给后面的服务，否则直接返回需要登录的相关错误码。
- `TransformGatewayFilterFactory` 这个过滤器提供了可选择的鉴权服务，用于处理一些登录态可选择性的业务场景。
  如果有`token`则可以得到对应的`userId`并传给下层服务，否则没有`userId`给下层服务。 底层服务根据是否有`userId`来进行对应的逻辑处理。


#### backend类的主要过滤器

- `BackendGatewayFilterFactory` 这个过滤器主要用于内部系统的鉴权, 
  通过获取`Authorization`头，然后转给`pigsy-auth`进行`jwt`模式验证，来判断是否正常登录，然后返回对应内部用户的相关信息

#### openapi类的主要过滤器

- `OpenapiGatewayFilterFactory` 这个过滤器为第三方外部服务提供入口，另外还可以通过`ipWhiteSet`配置相关接口的白名单的功能
  
### *网关调用外部服务*

在网关里面通过`WebClient.Builder`的异步方式来调用外部的`pigsy-auth`服务，主要是用于验证`header`里面的`token`，
验证成功的话，则把`token`转换成`userId`然后传给具体的服务。


# *关于*

# *License*

```
Copyright 2014-2021 Hunan Huinong Technology Co.,Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
