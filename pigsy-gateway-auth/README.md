# Pigsy-gateway-auth详细说明


## 核心用途

- 提供接口用于登录之后，生成对应的token给前端
- 提供接口用于校验用户端前端传入的token的有效性
- 提供接口用于校验内部系统端的token的有效性


## 工作流程

- 启动的时候注册到`eureka`
- `pigsy-gateway-core`模块对于需要验证token有效性的url先转发到该服务，根据该服务的返回进行后续逻辑处理
- 如果验证成功, 则把`token`转换成`userId`，然后传递到下层服务
- 如果验证失败，`pigsy-gateway-core`直接返回对应的错误码，要求前端重新登录